import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type, x-supabase-client-platform, x-supabase-client-platform-version, x-supabase-client-runtime, x-supabase-client-runtime-version',
};

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response(null, { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get('Authorization');
    if (!authHeader?.startsWith('Bearer ')) {
      return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
    }

    const supabaseUrl = Deno.env.get('SUPABASE_URL')!;
    const supabaseAnonKey = Deno.env.get('SUPABASE_ANON_KEY')!;
    const serviceRoleKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!;

    // Verify user is admin
    const userClient = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });

    const token = authHeader.replace('Bearer ', '');
    const { data: claimsData, error: claimsError } = await userClient.auth.getClaims(token);
    if (claimsError || !claimsData?.claims) {
      return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
    }

    const userId = claimsData.claims.sub;

    // Check admin role
    const adminClient = createClient(supabaseUrl, serviceRoleKey);
    const { data: roleData } = await adminClient
      .from('user_roles')
      .select('role')
      .eq('user_id', userId)
      .eq('role', 'admin')
      .maybeSingle();

    if (!roleData) {
      return new Response(JSON.stringify({ error: 'Forbidden: Admin only' }), { status: 403, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
    }

    const url = new URL(req.url);
    const action = url.searchParams.get('action');

    if (action === 'list-users') {
      const { data: { users }, error } = await adminClient.auth.admin.listUsers({ perPage: 100 });
      if (error) throw error;

      // Get roles for all users
      const { data: roles } = await adminClient.from('user_roles').select('user_id, role');
      const roleMap = new Map(roles?.map(r => [r.user_id, r.role]) || []);

      const enrichedUsers = users.map(u => ({
        id: u.id,
        email: u.email,
        full_name: u.user_metadata?.full_name || null,
        role: roleMap.get(u.id) || null,
        created_at: u.created_at,
        last_sign_in_at: u.last_sign_in_at,
        email_confirmed_at: u.email_confirmed_at,
      }));

      return new Response(JSON.stringify(enrichedUsers), { headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
    }

    if (action === 'update-role' && req.method === 'POST') {
      const { target_user_id, new_role } = await req.json();
      if (!target_user_id || !new_role) {
        return new Response(JSON.stringify({ error: 'Missing target_user_id or new_role' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
      }

      // Upsert role
      const { error } = await adminClient
        .from('user_roles')
        .upsert({ user_id: target_user_id, role: new_role }, { onConflict: 'user_id,role' });

      // If changing role, delete old role first then insert new
      await adminClient.from('user_roles').delete().eq('user_id', target_user_id);
      const { error: insertError } = await adminClient.from('user_roles').insert({ user_id: target_user_id, role: new_role });
      
      if (insertError) throw insertError;

      // Log audit event
      await adminClient.rpc('log_audit_event', {
        _user_id: userId,
        _action: 'role_changed',
        _entity_type: 'user',
        _entity_id: target_user_id,
        _details: { new_role },
      });

      return new Response(JSON.stringify({ success: true }), { headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
    }

    if (action === 'stats') {
      const [
        { count: userCount },
        { count: petCount },
        { count: requestCount },
        { count: activeRequestCount },
        { count: prescriptionCount },
      ] = await Promise.all([
        adminClient.from('profiles').select('*', { count: 'exact', head: true }),
        adminClient.from('pets').select('*', { count: 'exact', head: true }),
        adminClient.from('treatment_requests').select('*', { count: 'exact', head: true }),
        adminClient.from('treatment_requests').select('*', { count: 'exact', head: true }).in('status', ['submitted', 'assigned', 'in_progress']),
        adminClient.from('prescriptions').select('*', { count: 'exact', head: true }),
      ]);

      // Get requests by month (last 6 months)
      const sixMonthsAgo = new Date();
      sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);
      const { data: recentRequests } = await adminClient
        .from('treatment_requests')
        .select('created_at, status')
        .gte('created_at', sixMonthsAgo.toISOString())
        .order('created_at');

      // Get role distribution
      const { data: roleDistribution } = await adminClient
        .from('user_roles')
        .select('role');

      const roleCounts: Record<string, number> = {};
      roleDistribution?.forEach(r => { roleCounts[r.role] = (roleCounts[r.role] || 0) + 1; });

      return new Response(JSON.stringify({
        users: userCount || 0,
        pets: petCount || 0,
        requests: requestCount || 0,
        activeRequests: activeRequestCount || 0,
        prescriptions: prescriptionCount || 0,
        recentRequests: recentRequests || [],
        roleCounts,
      }), { headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
    }

    return new Response(JSON.stringify({ error: 'Unknown action' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
  } catch (error) {
    console.error('Admin API error:', error);
    return new Response(JSON.stringify({ error: error instanceof Error ? error.message : 'Internal server error' }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } });
  }
});
