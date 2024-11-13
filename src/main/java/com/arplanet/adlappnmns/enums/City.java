package com.arplanet.adlappnmns.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum City {

    KEL("KEL", "基隆市"),
    NTPC("NTPC","新北市"),
    TPE("TPE","臺北市"),
    TYN("TYN","桃園市"),
    HSZ("HSZ","新竹市"),
    HSZ_CO("HSZ","新竹縣"),
    ZMI("ZMI","苗栗市"),
    ZMI_CO("ZMI","苗栗縣"),
    TXG("TXG","臺中市"),
    CHW("CHW","彰化市"),
    CHW_CO("CHW","彰化縣"),
    NTC("NTC","南投市"),
    NTC_CO("NTC","南投縣"),
    YUN("YUN","雲林縣"),
    CYI("CYI","嘉義市"),
    CYI_CO("CYI","嘉義縣"),
    TNN("TNN","臺南市"),
    KHH("KHH","KHH"),
    PIF("PIF","屏東市"),
    PIF_CO("PIF","屏東縣"),
    ILA("ILA","宜蘭市"),
    ILA_CO("ILA","宜蘭縣"),
    HUN("HUN","花蓮市"),
    HUN_CO("HUN","花蓮縣"),
    TTT("TTT","臺東市"),
    TTT_CO("TTT","臺東縣"),
    PEH("PEH","澎湖縣"),
    GNI("GNI","綠島"),
    KYD("KYD","蘭嶼"),
    KNH("KNH","金門縣"),
    MFK("MFK","馬祖"),
    LNN ("LNN","連江縣"),
    FOR("FOR","海外");

    private final String cityCode;
    private final String cityName;

    City(String cityCode, String cityName) {
        this.cityCode = cityCode;
        this.cityName = cityName;
    }

    public static String getCodeByName(String cityName) {
        return Arrays.stream(City.values())
                .filter(city -> city.getCityName().equals(cityName))
                .map(City::getCityCode)
                .findFirst()
                .orElse(null);
    }
}
