package com.jkky98.spubg.pubg.enums;

import java.util.HashMap;
import java.util.Map;

public enum WeaponName {
    // 🔫 무기
    ACE32("ACE32"),
    AKM("AKM"),
    AUG_A3("AUG A3"),
    AWM("AWM"),
    S686("S686"),
    BERYL("Beryl"),
    BIZON("Bizon"),
    BLUEZONE_GRENADE("Bluezone Grenade"),
    FLASHBANG("Flash Bang"),
    C4("C4"),
    CROWBAR("Crowbar"),
    CROSSBOW("Crossbow"),
    DBS("DBS"),
    DP28("DP-28"),
    DEAGLE("Deagle"),
    DRAGUNOV("Dragunov"),
    G36C("G36C"),
    FRAG_GRENADE("Frag Grenade"),
    GROZA("Groza"),
    M416("M416"),
    JS9("JS9"),
    K2("K2"),
    KAR98K("Kar98k"),
    LYNX_AMR("Lynx AMR"),
    M16A4("M16A4"),
    P1911("P1911"),
    M249("M249"),
    M24("M24"),
    M79("M79"),
    P92("P92"),
    MG3("MG3"),
    MP5K("MP5K"),
    MP9("MP9"),
    MINI14("Mini 14"),
    MK12("Mk12"),
    MK14("Mk14"),
    MK47_MUTANT("Mk47 Mutant"),
    MOLOTOV_COCKTAIL("Molotov Cocktail"),
    MORTAR("Mortar"),
    MOSIN_NAGANT("Mosin-Nagant"),
    R1895("R1895"),
    O12("O12"),
    P90("P90"),
    PAN("Pan"),
    PANZERFAUST("Panzerfaust"),
    QBU88("QBU88"),
    QBZ95("QBZ95"),
    R45("R45"),
    SCAR_L("SCAR-L"),
    SKS("SKS"),
    S12K("S12K"),
    SAWED_OFF("Sawed-off"),
    SICKLE("Sickle"),
    SMOKE_GRENADE("Smoke Grenade"),
    SPIKE_TRAP("Spike Trap"),
    SPOTTER_SCOPE("Spotter Scope"),
    STICKY_BOMB("Sticky Bomb"),
    STUN_GUN("Stun Gun"),
    TACTICAL_PACK("Tactical Pack"),
    TOMMY_GUN("Tommy Gun"),
    UMP9("UMP9"),
    MICRO_UZI("Micro Uzi"),
    VSS("VSS"),
    VECTOR("Vector"),
    WIN94("Win94"),
    S1897("S1897"),
    SKORPION("Skorpion"),
    FLARE_GUN("Flare Gun"),
    MACHETE("Machete"),
    DP_28("DP 28"),
    SLR("SLR"),
    P18C("P18C"),
    MINI_14("Mini 14"),
    OTHER("OTHER");

    private final String displayName;
    private static final Map<String, WeaponName> WEAPON_MAP = new HashMap<>();

    static {
        String[][] mappings = {
                {"ProjC4_C", "C4"},
                {"ProjGrenade_C", "Frag Grenade"},
                {"ProjMolotov_C", "Molotov Cocktail"},
                {"ProjMolotov_DamageField_Direct_C", "Molotov Cocktail"},
                {"ProjStickyGrenade_C", "Sticky Bomb"},
                {"WeapACE32_C", "ACE32"},
                {"WeapAK47_C", "AKM"},
                {"WeapAUG_C", "AUG A3"},
                {"WeapAWM_C", "AWM"},
                {"WeapBerreta686_C", "S686"},
                {"WeapBerylM762_C", "Beryl"},
                {"WeapBizonPP19_C", "Bizon"},
                {"WeapCowbar_C", "Crowbar"},
                {"WeapCrossbow_1_C", "Crossbow"},
                {"WeapDP12_C", "DBS"},
                {"WeapDP28_C", "DP-28"},
                {"WeapDesertEagle_C", "Deagle"},
                {"WeapDragunov_C", "Dragunov"},
                {"WeapDuncansHK416_C", "M416"},
                {"WeapFNFal_C", "SLR"},
                {"WeapG18_C", "P18C"},
                {"WeapG36C_C", "G36C"},
                {"WeapGroza_C", "Groza"},
                {"WeapHK416_C", "M416"},
                {"WeapJS9_C", "JS9"},
                {"WeapJuliesKar98k_C", "Kar98k"},
                {"WeapK2_C", "K2"},
                {"WeapKar98k_C", "Kar98k"},
                {"WeapL6_C", "Lynx AMR"},
                {"WeapLunchmeatsAK47_C", "AKM"},
                {"WeapM16A4_C", "M16A4"},
                {"WeapM1911_C", "P1911"},
                {"WeapM249_C", "M249"},
                {"WeapM24_C", "M24"},
                {"WeapM9_C", "P92"},
                {"WeapMG3_C", "MG3"},
                {"WeapMP5K_C", "MP5K"},
                {"WeapMP9_C", "MP9"},
                {"WeapMacheteProjectile_C", "Machete"},
                {"WeapMachete_C", "Machete"},
                {"WeapMadsQBU88_C", "QBU88"},
                {"WeapMini14_C", "Mini 14"},
                {"WeapMk12_C", "Mk12"},
                {"WeapMk14_C", "Mk14"},
                {"WeapMk47Mutant_C", "Mk47 Mutant"},
                {"WeapMosinNagant_C", "Mosin-Nagant"},
                {"WeapNagantM1895_C", "R1895"},
                {"WeapOriginS12_C", "O12"},
                {"WeapP90_C", "P90"},
                {"WeapPan_C", "Pan"},
                {"WeapPanzerFaust100M1_C", "Panzerfaust"},
                {"WeapQBU88_C", "QBU88"},
                {"WeapQBZ95_C", "QBZ95"},
                {"WeapRhino_C", "R45"},
                {"WeapSCAR-L_C", "SCAR-L"},
                {"WeapSKS_C", "SKS"},
                {"WeapSaiga12_C", "S12K"},
                {"WeapSawnoff_C", "Sawed-off"},
                {"WeapSickle_C", "Sickle"},
                {"WeapThompson_C", "Tommy Gun"},
                {"WeapUMP_C", "UMP9"},
                {"WeapUZI_C", "Micro Uzi"},
                {"WeapVSS_C", "VSS"},
                {"WeapVector_C", "Vector"},
                {"WeapWin94_C", "Win94"},
                {"WeapWinchester_C", "S1897"},
                {"Weapvz61Skorpion_C", "Skorpion"},
                {"Item_Weapon_ACE32_C", "ACE32"},
                {"Item_Weapon_AK47_C", "AKM"},
                {"Item_Weapon_AUG_C", "AUG A3"},
                {"Item_Weapon_AWM_C", "AWM"},
                {"Item_Weapon_Berreta686_C", "S686"},
                {"Item_Weapon_BerylM762_C", "Beryl"},
                {"Item_Weapon_BizonPP19_C", "Bizon"},
                {"Item_Weapon_BluezoneGrenade_C", "Bluezone Grenade"},
                {"Item_Weapon_C4_C", "C4"},
                {"Item_Weapon_Cowbar_C", "Crowbar"},
                {"Item_Weapon_Crossbow_C", "Crossbow"},
                {"Item_Weapon_DP12_C", "DBS"},
                {"Item_Weapon_DP28_C", "DP-28"},
                {"Item_Weapon_DesertEagle_C", "Deagle"},
                {"Item_Weapon_Dragunov_C", "Dragunov"},
                {"Item_Weapon_Duncans_M416_C", "M416"},
                {"Item_Weapon_FNFal_C", "SLR"},
                {"Item_Weapon_FlareGun_C", "Flare Gun"},
                {"Item_Weapon_FlashBang_C", "Flashbang"},
                {"Item_Weapon_G18_C", "P18C"},
                {"Item_Weapon_G36C_C", "G36C"},
                {"Item_Weapon_Grenade_C", "Frag Grenade"},
                {"Item_Weapon_Grenade_Warmode_C", "Frag Grenade"},
                {"Item_Weapon_Groza_C", "Groza"},
                {"Item_Weapon_HK416_C", "M416"},
                {"Item_Weapon_JS9_C", "JS9"},
                {"Item_Weapon_K2_C", "K2"},
                {"Item_Weapon_Kar98k_C", "Kar98k"},
                {"Item_Weapon_L6_C", "Lynx AMR"},
                {"Item_Weapon_M16A4_C", "M16A4"},
                {"Item_Weapon_M1911_C", "P1911"},
                {"Item_Weapon_M249_C", "M249"},
                {"Item_Weapon_M24_C", "M24"},
                {"Item_Weapon_M79_C", "M79"},
                {"Item_Weapon_M9_C", "P92"},
                {"Item_Weapon_MG3_C", "MG3"},
                {"Item_Weapon_MP5K_C", "MP5K"},
                {"Item_Weapon_MP9_C", "MP9"},
                {"Item_Weapon_Machete_C", "Machete"},
                {"Item_Weapon_Mads_QBU88_C", "QBU88"},
                {"Item_Weapon_Mini14_C", "Mini 14"},
                {"Item_Weapon_Mk12_C", "Mk12"},
                {"Item_Weapon_Mk14_C", "Mk14"},
                {"Item_Weapon_Mk47Mutant_C", "Mk47 Mutant"},
                {"Item_Weapon_Molotov_C", "Molotov Cocktail"},
                {"Item_Weapon_Mortar_C", "Mortar"},
                {"Item_Weapon_Mosin_C", "Mosin-Nagant"},
                {"Item_Weapon_NagantM1895_C", "R1895"},
                {"Item_Weapon_OriginS12_C", "O12"},
                {"Item_Weapon_P90_C", "P90"},
                {"Item_Weapon_Pan_C", "Pan"},
                {"Item_Weapon_PanzerFaust100M_C", "Panzerfaust"},
                {"Item_Weapon_QBU88_C", "QBU88"},
                {"Item_Weapon_QBZ95_C", "QBZ95"},
                {"Item_Weapon_Rhino_C", "R45"},
                {"Item_Weapon_SCAR-L_C", "SCAR-L"},
                {"Item_Weapon_SKS_C", "SKS"},
                {"Item_Weapon_Saiga12_C", "S12K"},
                {"Item_Weapon_Sawnoff_C", "Sawed-off"},
                {"Item_Weapon_Sickle_C", "Sickle"},
                {"Item_Weapon_SmokeBomb_C", "Smoke Grenade"},
                {"Item_Weapon_SpikeTrap_C", "Spike Trap"},
                {"Item_Weapon_StickyGrenade_C", "Sticky Bomb"},
                {"Item_Weapon_StunGun_C", "Stun Gun"},
                {"Item_Weapon_Thompson_C", "Tommy Gun"},
                {"Item_Weapon_UMP_C", "UMP9"},
                {"Item_Weapon_UZI_C", "Micro Uzi"},
                {"Item_Weapon_VSS_C", "VSS"},
                {"Item_Weapon_Vector_C", "Vector"},
                {"Item_Weapon_Win1894_C", "Win94"},
                {"Item_Weapon_Winchester_C", "S1897"},
                {"Item_Weapon_vz61Skorpion_C", "Skorpion"},
        };

        for (String[] mapping : mappings) {
            WEAPON_MAP.put(mapping[0], WeaponName.valueOf(formatter(mapping[1])));
        }
    }

    WeaponName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static WeaponName fromKey(String key) {
        WeaponName weapon = WEAPON_MAP.getOrDefault(key, WeaponName.OTHER);

        // 대문자로 변환하고 공백과 '-'을 '_'로 변경
        String formattedName = weapon.getDisplayName()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        try {
            return WeaponName.valueOf(formattedName);
        } catch (IllegalArgumentException e) {
            return WeaponName.OTHER; // 존재하지 않는 경우 기본값 반환
        }
    }

    private static String formatter(String key) {
        return key.toUpperCase().replace(" ", "_").replace("-", "_");
    }
}
