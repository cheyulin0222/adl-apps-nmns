package com.arplanet.adlappnmns.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ProcessType {

    MATERIAL_INFO("material_info", "materialInfoService", "unit_content", true, false),
    USER_INFO("user_info", "userInfoService", "user", true, false),
    CLASS_INFO("class_info", "classInfoService", "user_class", true, false),
    GAME("game", "gameService", "game", true, false),
    ITEM_INFO("item_info", "itemInfoService", "quiz_question",  true, false),
    PAPER("paper", "paperService", "quiz",  true, false),
    PLATFORM_LOG("platform_log", "platformLogService", null,  false, false),
    GAME_PLAYING_TIME("game_playing_time", "gamePlayingTimeService", null,  false, false),
    GAME_PLAYING_LOG("game_playing_log", "gamePlayingLogService", null,  false, false),
    ASSESSMENT("assessment", "assessmentService", null,  false, false),
    ASSESSMENT_LOG("assessment_log", "assessmentLogService", null,  false, false),
    INSTRUCTION_DATA_LOG("instruction_data_log", "instructionDataLogService", null,  false, false),
    SESSION_INFO_LOG("session.info", "sessionInfoLogService", null,  false, true);

    private final String typeName;
    private final String nmnsService;
    private final String pathType;
    private final boolean enableS3Backup;
    private final boolean isPreContext;

    ProcessType(String typeName, String nmnsService, String pathType, boolean enableS3Backup, boolean isPreContext) {
        this.typeName = typeName;
        this.nmnsService = nmnsService;
        this.pathType = pathType;
        this.enableS3Backup = enableS3Backup;
        this.isPreContext = isPreContext;
    }

    public static ProcessType  getByNmnsService(String nmnsService) {
        return Arrays.stream(ProcessType.values())
                .filter(processType -> processType.getNmnsService().equals(nmnsService))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ProcessType found for nmnsService: " + nmnsService));
    }

}
