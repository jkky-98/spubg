package com.jkky98.spubg.pubg.request;

public enum TelemetryEventType {
    LOG_ARMOR_DESTROY("LogArmorDestroy"),
    LOG_BLACK_ZONE_ENDED("LogBlackZoneEnded"),
    LOG_CARE_PACKAGE_LAND("LogCarePackageLand"),
    LOG_CARE_PACKAGE_SPAWN("LogCarePackageSpawn"),
    LOG_CHARACTER_CARRY("LogCharacterCarry"),
    LOG_EM_PICKUP_LIFT_OFF("LogEmPickupLiftOff"),
    LOG_GAME_STATE_PERIODIC("LogGameStatePeriodic"),
    LOG_HEAL("LogHeal"),
    LOG_ITEM_ATTACH("LogItemAttach"),
    LOG_ITEM_DETACH("LogItemDetach"),
    LOG_ITEM_DROP("LogItemDrop"),
    LOG_ITEM_EQUIP("LogItemEquip"),
    LOG_ITEM_PICKUP("LogItemPickup"),
    LOG_ITEM_PICKUP_FROM_CAREPACKAGE("LogItemPickupFromCarepackage"),
    LOG_ITEM_PICKUP_FROM_CUSTOM_PACKAGE("LogItemPickupFromCustomPackage"),
    LOG_ITEM_PICKUP_FROM_LOOTBOX("LogItemPickupFromLootbox"),
    LOG_ITEM_PICKUP_FROM_VEHICLE_TRUNK("LogItemPickupFromVehicleTrunk"),
    LOG_ITEM_PUT_TO_VEHICLE_TRUNK("LogItemPutToVehicleTrunk"),
    LOG_ITEM_UNEQUIP("LogItemUnequip"),
    LOG_ITEM_USE("LogItemUse"),
    LOG_MATCH_DEFINITION("LogMatchDefinition"),
    LOG_MATCH_END("LogMatchEnd"),
    LOG_MATCH_START("LogMatchStart"),
    LOG_OBJECT_DESTROY("LogObjectDestroy"),
    LOG_OBJECT_INTERACTION("LogObjectInteraction"),
    LOG_PARACHUTE_LANDING("LogParachuteLanding"),
    LOG_PHASE_CHANGE("LogPhaseChange"),
    LOG_PLAYER_ATTACK("LogPlayerAttack"),
    LOG_PLAYER_CREATE("LogPlayerCreate"),
    LOG_PLAYER_DESTROY_BREACHABLE_WALL("LogPlayerDestroyBreachableWall"),
    LOG_PLAYER_DESTROY_PROP("LogPlayerDestroyProp"),
    LOG_PLAYER_KILL("LogPlayerKill"),
    LOG_PLAYER_KILL_V2("LogPlayerKillV2"),
    LOG_PLAYER_LOGIN("LogPlayerLogin"),
    LOG_PLAYER_LOGOUT("LogPlayerLogout"),
    LOG_PLAYER_MAKE_GROGGY("LogPlayerMakeGroggy"),
    LOG_PLAYER_POSITION("LogPlayerPosition"),
    LOG_PLAYER_REDEPLOY("LogPlayerRedeploy"),
    LOG_PLAYER_REDEPLOY_BR_START("LogPlayerRedeployBRStart"),
    LOG_PLAYER_REVIVE("LogPlayerRevive"),
    LOG_PLAYER_TAKE_DAMAGE("LogPlayerTakeDamage"),
    LOG_PLAYER_USE_FLARE_GUN("LogPlayerUseFlareGun"),
    LOG_PLAYER_USE_THROWABLE("LogPlayerUseThrowable"),
    LOG_RED_ZONE_ENDED("LogRedZoneEnded"),
    LOG_SWIM_END("LogSwimEnd"),
    LOG_SWIM_START("LogSwimStart"),
    LOG_VAULT_START("LogVaultStart"),
    LOG_VEHICLE_DAMAGE("LogVehicleDamage"),
    LOG_VEHICLE_DESTROY("LogVehicleDestroy"),
    LOG_VEHICLE_LEAVE("LogVehicleLeave"),
    LOG_VEHICLE_RIDE("LogVehicleRide"),
    LOG_WEAPON_FIRE_COUNT("LogWeaponFireCount"),
    LOG_WHEEL_DESTROY("LogWheelDestroy");

    private final String eventName;

    TelemetryEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
