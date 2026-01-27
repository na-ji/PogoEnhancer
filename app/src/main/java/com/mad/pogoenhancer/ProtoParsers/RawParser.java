package com.mad.pogoenhancer.ProtoParsers;

import android.util.Pair;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.gpx.GpxManager;
import com.mad.pogoenhancer.services.HookReceiverService;
import com.mad.pogoenhancer.services.UnixSender;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import POGOProtos.Rpc.ActionLogEntry;
import POGOProtos.Rpc.CatchPokemonOutProto;
import POGOProtos.Rpc.ClientQuestProto;
import POGOProtos.Rpc.DiskEncounterOutProto;
import POGOProtos.Rpc.EncounterOutProto;
import POGOProtos.Rpc.FortSearchOutProto;
import POGOProtos.Rpc.GetActionLogResponse;
import POGOProtos.Rpc.GetMapObjectsOutProto;
import POGOProtos.Rpc.GetQuestDetailsOutProto;
import POGOProtos.Rpc.GetRaidDetailsOutProto;
import POGOProtos.Rpc.GetRoutesOutProto;
import POGOProtos.Rpc.IncenseEncounterOutProto;
import POGOProtos.Rpc.Method;
import POGOProtos.Rpc.PokemonProto;
import POGOProtos.Rpc.QuestEncounterOutProto;


public class RawParser {
    public final static String TAG = "PogoDroid";
    public static long raidSeed = 0;
    public static long lastLogCheck = System.currentTimeMillis();

    public static void parse(String raw, HookReceiverService _hookService) throws InvalidProtocolBufferException, JSONException {
        if (!raw.startsWith("raw;")) {
            return;
        }

        Pair<Long, Pair<Integer, byte[]>> methodBytePair = rawStringToByte(raw);
        if (methodBytePair == null) {
            return;
        }
        switch (methodBytePair.second.first) {
            case Method.METHOD_SFIDA_ACTION_VALUE:
                GetActionLogResponse sfidaActionLogResponse = GetActionLogResponse.parseFrom(methodBytePair.second.second);
                scanActionLog(sfidaActionLogResponse);
                break;

            case Method.METHOD_GET_MAP_OBJECTS_VALUE:
                GetMapObjectsOutProto mapResponseProto = GetMapObjectsOutProto.parseFrom(methodBytePair.second.second);
                Logger.debug("ProtoHookJ", "GMO Status: " + mapResponseProto.getStatusValue());
                if (mapResponseProto.getMapCellCount() > 0) {
                    GpxManager.getInstance(_hookService).updateGmo(mapResponseProto);
                    _hookService.updateNearby(mapResponseProto);
                }
                break;

            case Method.METHOD_GIFT_DETAILS_VALUE:
                break;
            case Method.METHOD_EVOLVE_POKEMON_VALUE:
                break;
                /*
                EvolvePokemonResponse evolvePokemonResponse = EvolvePokemonResponse.parseFrom(methodBytePair.second.second);
                long PokemonId = evolvePokemonResponse.getEvolvedPokemonData().getId();
                int Status = evolvePokemonResponse.getResult().getNumber();
                if(Status == 1) {
                    Logger.info("PogoEnhancerJ", "Transfer evolved Mon: " + PokemonId);

                    JSONObject obj = new JSONObject();
                    JSONObject settingObj = new JSONObject();
                    try {
                        obj.put("newmonid", PokemonId);
                        settingObj.put("transferoncatch", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    UnixSender.sendMessage(settingObj.toString());
                }
                break;
                */
            case Method.METHOD_ENCOUNTER_VALUE:
                EncounterOutProto encounterResponseProto = EncounterOutProto.parseFrom(methodBytePair.second.second);
                _hookService.addEncountered(encounterResponseProto.getPokemon().getEncounterId());
                break;
            case Method.METHOD_FORT_DETAILS_VALUE:
                break;
            case Method.METHOD_GYM_GET_INFO_VALUE:
                break;
            case Method.METHOD_DOWNLOAD_SETTINGS_VALUE:
                break;
            case Method.METHOD_GET_HATCHED_EGGS_VALUE:
                break;
            case Method.METHOD_RECYCLE_INVENTORY_ITEM_VALUE:
                break;
            case Method.METHOD_GET_HOLOHOLO_INVENTORY_VALUE:
                break;
            case Method.METHOD_FORT_SEARCH_VALUE:
                break;
            case Method.METHOD_GET_QUEST_DETAILS_VALUE:
                GetQuestDetailsOutProto getQuestDetailsResponse = GetQuestDetailsOutProto.parseFrom(methodBytePair.second.second);
                JSONArray questsArr = new JSONArray();
                List<ClientQuestProto> questsList =
                        getQuestDetailsResponse.getQuestsList();
                JSONObject obj = new JSONObject();

                for (ClientQuestProto clientQuest : questsList) {
                    if (clientQuest.getQuest().getQuestContextValue() == 2) {
                        // questsArr.put(new ClientQuest(clientQuest).toString());
                        Logger.debug("PROTO", clientQuest.getQuest().getQuestId());
                        String questID = clientQuest.getQuest().getQuestId();
                        int questContextValue = clientQuest.getQuest().getQuestContextValue();
                        int questTypeValue = clientQuest.getQuest().getQuestTypeValue();

                        obj.put("questContextValue", questContextValue);
                        obj.put("questTypeValue", questTypeValue);
                        obj.put("questID", questID);
                        questsArr.put(obj);
                    }
                }

                Logger.debug("PROTO", questsArr.toString());
                Logger.debug("PROTO", "TEST: " + questsList.size());
                JSONObject settingObj = new JSONObject();
                try {
                    settingObj.put("quests", questsArr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                UnixSender.sendMessage(settingObj.toString());
                break;
            case Method.METHOD_GET_NEW_QUESTS_VALUE:
                /*
                GetNewQuestsResponse getNewQuestsResponse = GetNewQuestsResponse.parseFrom(methodBytePair.second.second);
                JSONArray questsArr = new JSONArray();
                List<POGOProtos.Data.Quests.ClientQuest> questsList =
                        getNewQuestsResponse.getQuestsList();
                for (POGOProtos.Data.Quests.ClientQuest clientQuest : questsList) {
                    Logger.debug("PROTO", clientQuest.toString());
                    questsArr.put(new ClientQuest(clientQuest).toJsonObject());
                }
                Logger.debug("PROTO", questsArr.toString());
                Logger.debug("PROTO", "TEST: " + questsList.size());
                Logger.info("PogoEnhancerJ", getNewQuestsResponse.getQuestsList().toString());
                break;
*/




            case Method.METHOD_GET_PLAYER_VALUE:
                // TODO: use level
                break;
            case Method.METHOD_UNSET_VALUE:
                bruteForceMethod(methodBytePair.second.second, _hookService);
                break;
            case Method.METHOD_FETCH_ALL_NEWS_VALUE:
                break;
            case Method.METHOD_INCENSE_ENCOUNTER_VALUE:
                IncenseEncounterOutProto incenseEncounterResponseProto = IncenseEncounterOutProto.parseFrom(methodBytePair.second.second);
                showIvOverlay(incenseEncounterResponseProto.getPokemon(), _hookService);
                break;
            case Method.METHOD_DISK_ENCOUNTER_VALUE: //DISK and INCENSE Encounters are equal..
                //we just want to display IVs ;)
                DiskEncounterOutProto diskEncounterResponseProto = DiskEncounterOutProto.parseFrom(methodBytePair.second.second);
                showIvOverlay(diskEncounterResponseProto.getPokemon(), _hookService);
                break;
            case Method.METHOD_QUEST_ENCOUNTER_VALUE:
                //we just want to display IVs ;)
                QuestEncounterOutProto questEncounterResponseProto = QuestEncounterOutProto.parseFrom(methodBytePair.second.second);
                showIvOverlay(questEncounterResponseProto.getPokemon(), _hookService);
                break;
            case Method.METHOD_GET_RAID_DETAILS_VALUE:
                //we just want to display IVs ;)
                GetRaidDetailsOutProto raidDetailsResponseProto = GetRaidDetailsOutProto.parseFrom(methodBytePair.second.second);
                if (raidDetailsResponseProto != null && raidDetailsResponseProto.hasRaidInfo()
                        && raidDetailsResponseProto.getRaidInfo().hasRaidPokemon() &&
                        (raidDetailsResponseProto.getRaidInfo().getRaidSeed() != raidSeed)) {
                    showMovesetsAndInfo(_hookService, raidDetailsResponseProto.getRaidInfo().getRaidPokemon());
                    raidSeed = raidDetailsResponseProto.getRaidInfo().getRaidSeed();
                }

                break;
            case Method.METHOD_CATCH_POKEMON_VALUE:
                CatchPokemonOutProto catchPokemonResponseProto = CatchPokemonOutProto.parseFrom(methodBytePair.second.second);
                Logger.info("PogoEnhancerJ", String.valueOf(catchPokemonResponseProto.getCapturedPokemonId()));
                notifyCaptureResult(catchPokemonResponseProto, _hookService);
                break;
            case Method.METHOD_GET_ROUTES_VALUE:
                GetRoutesOutProto getRoutesOutProto = GetRoutesOutProto.parseFrom(methodBytePair.second.second);
                Logger.info("ROUTE", String.valueOf(getRoutesOutProto.getRouteMapCellCount()));
                if (!getRoutesOutProto.getRouteMapCellList().isEmpty()) {
                    GpxManager.getInstance(_hookService).updateNearbyRoutes(getRoutesOutProto, _hookService.getSharedLatLon());
                }
                break;
            default:
                break;
        }
    }

    private static void showMovesetsAndInfo(HookReceiverService hookService, PokemonProto raid_pokemon) {
        // TODO: consider translating and stuff..
        if (raid_pokemon == null) return;

            int monId = raid_pokemon.getPokemonIdValue();
            int move_1 = raid_pokemon.getMove1Value();
            int move_2 = raid_pokemon.getMove2Value();

            hookService.notifyIdAndMoveset(monId, move_1, move_2);

    }

    private static boolean bruteForceMethod(byte[] rawData, HookReceiverService _hookService) {
        /*
        Let's try to parse any unknown methods that we may know to be unknown from time to time
        Ugly code, but hey..
         */
        try {
            EncounterOutProto encounterResponseProto = EncounterOutProto.parseFrom(rawData);
            if (encounterResponseProto != null && encounterResponseProto.hasPokemon()
                    && encounterResponseProto.getPokemon().hasPokemon()
                    && encounterResponseProto.getPokemon().getEncounterId() != 0) {
                showIvOverlay(encounterResponseProto.getPokemon().getPokemon(), _hookService);
                return true;
            }
        } catch (InvalidProtocolBufferException e) {
            Logger.debug("PogoEnhancerJ", "Not an encounter response");
        }

        try {
            FortSearchOutProto fortSearchResponseProto = FortSearchOutProto.parseFrom(rawData);
            if (fortSearchResponseProto != null
                    && !fortSearchResponseProto.getFortId().isEmpty()) {
                return true;
            }
        } catch (InvalidProtocolBufferException e) {
            Logger.debug("PogoEnhancerJ", "Not an encounter response");
        }

        return false;
    }

    private static void scanActionLog(GetActionLogResponse sfidaActionLogResponse) {
        List<ActionLogEntry> logEntries = sfidaActionLogResponse.getLogList();
        for (ActionLogEntry entry : logEntries) {
            long lastusage = entry.getTimestampMs();

            if (lastusage > lastLogCheck) {
                long pokemonId = entry.getCatchPokemon().getPokemonId();
                int catchStatus = entry.getCatchPokemon().getResultValue();
                if (catchStatus == 1) {
                    Logger.info("PogoEnhancerJ", "Transfer Mon: " + String.valueOf(pokemonId));

                    JSONObject obj = new JSONObject();
                    JSONObject settingObj = new JSONObject();
                    try {
                        obj.put("newmonid", pokemonId);
                        settingObj.put("transferoncatch", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    UnixSender.sendMessage(settingObj.toString());
                }
                lastLogCheck = lastusage;
            }
        }
    }

    private static void notifyCaptureResult(CatchPokemonOutProto values, HookReceiverService _hookService) {
        if (values == null) {
            return;
        }

        int catchStatusRaw = values.getStatusValue();

        CatchPokemonOutProto.Status catchStatus;
        switch (catchStatusRaw) {
            case 1:
                JSONObject obj = new JSONObject();
                JSONObject settingObj = new JSONObject();
                try {
                    obj.put("newmonid", values.getCapturedPokemonId());
                    settingObj.put("transferoncatch", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                UnixSender.sendMessage(settingObj.toString());
                catchStatus = CatchPokemonOutProto.Status.CATCH_SUCCESS;
                break;
            case 2:
                catchStatus = CatchPokemonOutProto.Status.CATCH_ESCAPE;
                break;
            case 3:
                catchStatus = CatchPokemonOutProto.Status.CATCH_FLEE;
                break;
            case 4:
                catchStatus = CatchPokemonOutProto.Status.CATCH_MISSED;
                break;
            case 0:
            default:
                catchStatus = CatchPokemonOutProto.Status.CATCH_ERROR;
        }

        _hookService.notifyCaptureResult(catchStatus);
    }

    private static void showIvOverlay(PokemonProto values, HookReceiverService _hookService) {
        if (values == null) {
            return;
        }

        int ditto = 0;
        int attack = values.getIndividualAttack();
        int stamina = values.getIndividualStamina();
        int defence = values.getIndividualDefense();
        double cpMultiplier = values.getCpMultiplier();
        double additionalCpMultiplier = values.getAdditionalCpMultiplier();
        boolean shiny = values.getPokemonDisplay().getShiny();
        int gender_value = values.getPokemonDisplay().getGenderValue();
        int weather_value = values.getPokemonDisplay().getWeatherBoostedConditionValue();

        _hookService.showIvOverlay(attack, defence, stamina, cpMultiplier, additionalCpMultiplier, 0, shiny ? 1 : 0, 1, gender_value, weather_value, "", "", ditto);
    }

    private static Pair<Long, Pair<Integer, byte[]>> rawStringToByte(String rawString) {
        String[] split = rawString.split(";");
        long timestamp = Long.parseLong(split[1]);
        int method = Integer.parseInt(split[2]);

        String[] uints = split[3].split(",");
        StringBuilder hexCrap = new StringBuilder();
        for (String uint : uints) {
            String part = "";
            try {
                part = Integer.toHexString(Integer.parseInt(uint));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                return null;
            }
            if (part.length() == 1) {
                part = "0" + part; //padding in front, we do not want to loose any information ;)
            }
            hexCrap.append(part);
        }
        String test = "";
        byte[] raw = hexStringToByteArray(hexCrap.toString());
        return new Pair<Long, Pair<Integer, byte[]>>(timestamp, new Pair<Integer, byte[]>(method, raw));
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] data = new byte[s.length() / 2];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) ((Character.digit(s.charAt(i * 2), 16) << 4)
                    + Character.digit(s.charAt(i * 2 + 1), 16));
        }
        return data;
    }
}

