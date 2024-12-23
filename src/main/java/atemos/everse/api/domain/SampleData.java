package atemos.everse.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SampleData {
    @Getter
    @AllArgsConstructor
    public enum ParentMenu {
        MAIN("Main", "/", "Provides a centralized dashboard with essential insights and tools.", new HashSet<>(Set.of(MemberRole.ADMIN, MemberRole.MANAGER, MemberRole.USER)), null),
        ENERGY_MONITORING("Energy Monitoring", "/energy", "Offers real-time monitoring of energy consumption and usage patterns.", new HashSet<>(Set.of(MemberRole.ADMIN, MemberRole.MANAGER, MemberRole.USER)), null),
        AI_ANALYTICS("AI Analytics", "/ai", "Delivers advanced analytics powered by AI for deeper energy data analysis.", new HashSet<>(Set.of(MemberRole.ADMIN, MemberRole.MANAGER)), SubscriptionServiceList.AI_ENERGY_USAGE_FORECAST),
        MANAGEMENT("Management", "/management", "Handles user management and financial operations.", new HashSet<>(Set.of(MemberRole.ADMIN, MemberRole.MANAGER)), null);

        private final String name;
        private final String path;
        private final String description;
        private final Set<MemberRole> accessibleRoles;
        private final SubscriptionServiceList requiredSubscription;
    }

    @Getter
    @AllArgsConstructor
    public enum ChildrenMenu {
        ENERGY_USAGE("Energy Usage", "/energy/usage", "Tracks and displays detailed energy consumption metrics.", ParentMenu.ENERGY_MONITORING, ParentMenu.ENERGY_MONITORING.accessibleRoles, null),
        BILLING_OVERVIEW("Billing Overview", "/energy/billing-overview", "Provides a summary of current and past energy bills.", ParentMenu.ENERGY_MONITORING, ParentMenu.ENERGY_MONITORING.accessibleRoles, null),
        IOT_DEVICES("IoT Devices", "/energy/iot-devices", "Monitors the status and performance of connected IoT devices.", ParentMenu.ENERGY_MONITORING, ParentMenu.ENERGY_MONITORING.accessibleRoles, null),
        //USAGE_PATTERNS("Usage Patterns", "/ai/usage-pattern", "Analyzes energy usage patterns to forecast future consumption.", ParentMenu.AI_ANALYTICS, ParentMenu.AI_ANALYTICS.accessibleRoles, SubscriptionServiceList.AI_ENERGY_USAGE_FORECAST),
        AI_PEAK_MANAGEMENT("Peak Management", "/ai/peak-management", "Optimizes energy usage during peak hours to reduce costs.", ParentMenu.AI_ANALYTICS, ParentMenu.AI_ANALYTICS.accessibleRoles, SubscriptionServiceList.AI_ENERGY_USAGE_FORECAST),
        ANOMALY_DETECTION("Anomaly Detection", "/ai/anomaly-detection", "Detects and alerts on unusual energy consumption patterns.", ParentMenu.AI_ANALYTICS, ParentMenu.AI_ANALYTICS.accessibleRoles, SubscriptionServiceList.AI_ENERGY_USAGE_FORECAST),
        SAVING_REPORT("Saving Report", "/ai/saving-report", "Generates reports on potential energy savings based on usage data.", ParentMenu.AI_ANALYTICS, ParentMenu.AI_ANALYTICS.accessibleRoles, SubscriptionServiceList.AI_ENERGY_USAGE_FORECAST),
        COMPANY("Company", "/management/company", "Manage and operate company info.", ParentMenu.MANAGEMENT, new HashSet<>(Set.of(MemberRole.ADMIN)), null),
        MEMBER("Member", "/management/member", "Administers user roles, permissions, and accounts.", ParentMenu.MANAGEMENT, ParentMenu.MANAGEMENT.accessibleRoles, null),
        USAGE_HISTORY("Usage History", "/management/usage-history", "Displays the history of service usage and payment transactions.", ParentMenu.MANAGEMENT, new HashSet<>(Set.of(MemberRole.MANAGER)), null);

        private final String name;
        private final String path;
        private final String description;
        private final ParentMenu parentMenu;
        private final Set<MemberRole> accessibleRoles;
        private final SubscriptionServiceList requiredSubscription;
    }

    @Getter
    @AllArgsConstructor
    public enum Country {
        KOREA("Korea", "ko-KR", "Asia/Seoul"),
        USA("USA", "en-US", "America/New_York"),
        THAILAND("Thailand", "th-TH", "Asia/Bangkok"),
        VIETNAM("Vietnam", "vi-VN", "Asia/Ho_Chi_Minh");
        private final String name;
        private final String languageCode;
        private final String timeZone;
    }

    @Getter
    @AllArgsConstructor
    public enum EnergyRate {
        KOREA(
                new BigDecimal("0.0841"),
                new BigDecimal("0.0782"),
                new BigDecimal("1.75"),
                List.of(10, 11, 17, 18, 19),
                new BigDecimal("1.35"),
                List.of(8, 9, 12, 13, 14, 15, 16),
                new BigDecimal("0.65"),
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 20, 21, 22, 23)
        ),
        USA(
                new BigDecimal("0.0743"),
                new BigDecimal("0.1286"),
                new BigDecimal("1.75"),
                List.of(7, 8, 16, 17, 18, 19, 20),
                new BigDecimal("1.3"),
                List.of(9, 10, 11, 12, 13, 14, 15, 21, 22, 23),
                new BigDecimal("0.6"),
                List.of(0, 1, 2, 3, 4, 5, 6)
        ),
        THAILAND(
                new BigDecimal("0.1014"),
                new BigDecimal("0.1124"),
                new BigDecimal("1.65"),
                List.of(13, 14, 15),
                new BigDecimal("1.25"),
                List.of(8, 9, 10, 11, 12, 16, 17, 18, 19, 20, 21),
                new BigDecimal("0.7"),
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 22, 23)
        ),
        VIETNAM(
                new BigDecimal("0.1052"),
                new BigDecimal("0.1111"),
                new BigDecimal("1.75"),
                List.of(9, 10, 11, 17, 18, 19),
                new BigDecimal("1.3"),
                List.of(4, 5, 6, 7, 8, 12, 13, 14, 15, 16, 20, 21, 22, 23),
                new BigDecimal("0.7"),
                List.of(0, 1, 2, 3)
        );
        private final BigDecimal industrialRate;
        private final BigDecimal commercialRate;
        private final BigDecimal peakMultiplier;
        private final List<Integer> peakHours;
        private final BigDecimal midPeakMultiplier;
        private final List<Integer> midPeakHours;
        private final BigDecimal offPeakMultiplier;
        private final List<Integer> offPeakHours;
    }

    @Getter
    @AllArgsConstructor
    public enum Company {
        KOREA("ATEMoS Korea", "korea@atemos.co.kr", "01012345678", "01012345679", "Korea address", CompanyType.FEMS, Country.KOREA.getName()),
        USA("ATEMoS USA", "usa@atemos.com", "1234567890", "1234567890", "USA address", CompanyType.BEMS, Country.USA.getName()),
        THAILAND("ATEMoS Thailand", "thailand@atemos.co.th", "08161234567", "08161234569", "Thailand address", CompanyType.FEMS, Country.THAILAND.getName()),
        VIETNAM("ATEMoS Vietnam", "vietnam@atemos.vn", "09123456789", "09123456781", "Vietnam address", CompanyType.BEMS, Country.VIETNAM.getName());

        private final String name;
        private final String email;
        private final String tel;
        private final String fax;
        private final String address;
        private final CompanyType type;
        private final String countryName;
    }

    @Getter
    @AllArgsConstructor
    public enum Member {
        KOREA_ADMIN(Company.KOREA.getName(), "atemos@atemos.co.kr", "01012349876", "Atemos1234!", MemberRole.ADMIN),
        USA_MANAGER(Company.USA.getName(), "usa@atemos.co.kr", "1234567890", "Atemos1234!", MemberRole.MANAGER),
        THAILAND_MANAGER(Company.THAILAND.getName(), "th@atemos.co.kr", "0812345678", "Atemos1234!", MemberRole.MANAGER),
        VIETNAM_MANAGER(Company.VIETNAM.getName(), "vn@atemos.co.kr", "09123456789", "Atemos1234!", MemberRole.MANAGER);

        private final String name;
        private final String email;
        private final String phone;
        private final String password;
        private final MemberRole role;
    }

    @Getter
    @AllArgsConstructor
    public enum Iot {
        KOREA_IOT_1("atemos-kr-1234-1", IotType.AIR_CONDITIONER, "Seoul Sector 1", new BigDecimal("59.14"), Company.KOREA.getName()),
        KOREA_IOT_2("atemos-kr-1234-2", IotType.MOTOR, "Busan Sector 2", new BigDecimal("60.00"), Company.KOREA.getName()),
        KOREA_IOT_3("atemos-kr-1234-3", IotType.RADIATOR, "Jeju Sector 3", new BigDecimal("65.50"), Company.KOREA.getName()),
        USA_IOT_1("atemos-us-1234-1", IotType.AIR_CONDITIONER, "New York Sector 1", new BigDecimal("75.30"), Company.USA.getName()),
        USA_IOT_2("atemos-us-1234-2", IotType.MOTOR, "New York Sector 2", new BigDecimal("80.00"), Company.USA.getName()),
        THAILAND_IOT_1("atemos-th-1234-1", IotType.RADIATOR, "Bangkok Sector 1", new BigDecimal("60.50"), Company.THAILAND.getName()),
        VIETNAM_IOT_1("atemos-vn-1234-1", IotType.MOTOR, "Hanoi Sector 1", new BigDecimal("55.75"), Company.VIETNAM.getName());

        private final String serialNumber;
        private final IotType type;
        private final String location;
        private final BigDecimal price;
        private final String CompanyName;
    }


    @Getter
    @AllArgsConstructor
    public enum Anomaly {
        KOREA(new BigDecimal("120"), new BigDecimal("1200"), Company.KOREA.getName()),
        USA(new BigDecimal("80"), new BigDecimal("800"), Company.USA.getName()),
        THAILAND(new BigDecimal("40"), new BigDecimal("400"), Company.THAILAND.getName()),
        VIETNAM(new BigDecimal("40"), new BigDecimal("400"), Company.VIETNAM.getName());
        private final BigDecimal lowestHourlyEnergyUsage;
        private final BigDecimal highestHourlyEnergyUsage;
        private final String companyName;
    }
}