package com.genalpha.cricketacademy.data

object SupabaseConfig {
    // The shared Academy Manager platform, not GenAlpha's old project.
    // GenAlpha is now a tenant alongside raj, leo, mpp, matchpoint and
    // demo; its tables live as views in the `genalpha` schema over the
    // shared ones, which is why every request pins that schema (see
    // SupabaseRepository.baseRequest).
    //
    // Until v1.0.58 this pointed at hwxhigwaklzedxufwedv, so the app was
    // reading and writing a database nobody else reads.
    const val URL = "https://ugsklcipzyiogxynshnh.supabase.co"
    const val ANON_KEY = "sb_publishable_Lrxh3RceGcj7g5JEefze_g_R-bMtAn3"

    // Bumped when a release must not keep running. The app compares its
    // own versionCode against tenants.config.minAppVersion on launch.
    const val MIN_SUPPORTED_VERSION_KEY = "minAppVersion"
}
