package com.jkky98.spubg.pubg.enums;

import java.util.HashMap;
import java.util.Map;

public enum WeaponType {
    AR,
    DMR,
    SR,
    SMG,
    SHOTGUN,
    THROWABLE,
    OTHER;

    private static final Map<WeaponName, WeaponType> TYPE_MAP = new HashMap<>();

    static {
        // 돌격소총 (AR)
        TYPE_MAP.put(WeaponName.ACE32, AR);
        TYPE_MAP.put(WeaponName.AKM, AR);
        TYPE_MAP.put(WeaponName.AUG_A3, AR);
        TYPE_MAP.put(WeaponName.BERYL, AR);
        TYPE_MAP.put(WeaponName.G36C, AR);
        TYPE_MAP.put(WeaponName.GROZA, AR);
        TYPE_MAP.put(WeaponName.M416, AR);
        TYPE_MAP.put(WeaponName.K2, AR);
        TYPE_MAP.put(WeaponName.M16A4, AR);
        TYPE_MAP.put(WeaponName.MK47_MUTANT, AR);
        TYPE_MAP.put(WeaponName.QBZ95, AR);
        TYPE_MAP.put(WeaponName.SCAR_L, AR);

        // 지정사수소총 (DMR)
        TYPE_MAP.put(WeaponName.DRAGUNOV, DMR);
        TYPE_MAP.put(WeaponName.MINI14, DMR);
        TYPE_MAP.put(WeaponName.MK12, DMR);
        TYPE_MAP.put(WeaponName.MK14, DMR);
        TYPE_MAP.put(WeaponName.QBU88, DMR);
        TYPE_MAP.put(WeaponName.SKS, DMR);

        // 저격소총 (SR)
        TYPE_MAP.put(WeaponName.AWM, SR);
        TYPE_MAP.put(WeaponName.KAR98K, SR);
        TYPE_MAP.put(WeaponName.LYNX_AMR, SR);
        TYPE_MAP.put(WeaponName.M24, SR);
        TYPE_MAP.put(WeaponName.MOSIN_NAGANT, SR);
        TYPE_MAP.put(WeaponName.WIN94, SR);

        // 기관단총 (SMG)
        TYPE_MAP.put(WeaponName.BIZON, SMG);
        TYPE_MAP.put(WeaponName.JS9, SMG);
        TYPE_MAP.put(WeaponName.MP5K, SMG);
        TYPE_MAP.put(WeaponName.MP9, SMG);
        TYPE_MAP.put(WeaponName.P90, SMG);
        TYPE_MAP.put(WeaponName.TOMMY_GUN, SMG);
        TYPE_MAP.put(WeaponName.UMP9, SMG);
        TYPE_MAP.put(WeaponName.MICRO_UZI, SMG);
        TYPE_MAP.put(WeaponName.VECTOR, SMG);
        TYPE_MAP.put(WeaponName.SKORPION, SMG);

        // 샷건 (SHOTGUN)
        TYPE_MAP.put(WeaponName.S686, SHOTGUN);
        TYPE_MAP.put(WeaponName.DBS, SHOTGUN);
        TYPE_MAP.put(WeaponName.O12, SHOTGUN);
        TYPE_MAP.put(WeaponName.S12K, SHOTGUN);
        TYPE_MAP.put(WeaponName.SAWED_OFF, SHOTGUN);
        TYPE_MAP.put(WeaponName.S1897, SHOTGUN);

        // 투척류 (THROWABLE)
        TYPE_MAP.put(WeaponName.BLUEZONE_GRENADE, THROWABLE);
        TYPE_MAP.put(WeaponName.C4, THROWABLE);
        TYPE_MAP.put(WeaponName.FRAG_GRENADE, THROWABLE);
        TYPE_MAP.put(WeaponName.MOLOTOV_COCKTAIL, THROWABLE);
        TYPE_MAP.put(WeaponName.SMOKE_GRENADE, THROWABLE);
        TYPE_MAP.put(WeaponName.STICKY_BOMB, THROWABLE);
        TYPE_MAP.put(WeaponName.STUN_GUN, THROWABLE);

        // 기타 무기 (OTHER)
        TYPE_MAP.put(WeaponName.FLARE_GUN, OTHER);
        TYPE_MAP.put(WeaponName.M249, OTHER);
        TYPE_MAP.put(WeaponName.M79, OTHER);
        TYPE_MAP.put(WeaponName.MG3, OTHER);
        TYPE_MAP.put(WeaponName.PAN, OTHER);
        TYPE_MAP.put(WeaponName.PANZERFAUST, OTHER);
        TYPE_MAP.put(WeaponName.SICKLE, OTHER);
        TYPE_MAP.put(WeaponName.SPIKE_TRAP, OTHER);
        TYPE_MAP.put(WeaponName.SPOTTER_SCOPE, OTHER);
        TYPE_MAP.put(WeaponName.TACTICAL_PACK, OTHER);
        TYPE_MAP.put(WeaponName.MORTAR, OTHER);
    }

    public static WeaponType getWeaponType(WeaponName weaponName) {
        return TYPE_MAP.getOrDefault(weaponName, OTHER);
    }
}
