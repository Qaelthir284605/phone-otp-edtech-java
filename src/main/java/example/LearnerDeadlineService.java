package example;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class LearnerDeadlineService {
    private static final String SEND_CODE_CAPABILITY = "auth.phone.send_code";
    private final InfraiClient infrai;

    public LearnerDeadlineService(InfraiClient infrai) { this.infrai = infrai; }

    public String sendCode(String phone) throws IOException, InterruptedException {
        return infrai.post("/v1/auth/phone/send_code", "{\"phone\":\"" + phone + "\",\"purpose\":\"login\",\"locale\":\"en-US\"}");
    }

    public String verifyCode(String phone, String code) throws IOException, InterruptedException {
        return infrai.post("/v1/auth/phone/verify", "{\"phone\":\"" + phone + "\",\"code\":\"" + code + "\",\"login\":true}");
    }

    public DeadlineDecision deadline(String learnerId, LocalDate dueDate, LocalDate today) {
        long days = ChronoUnit.DAYS.between(today, dueDate);
        if (days < 0) return new DeadlineDecision(learnerId, "OVERDUE", days);
        if (days <= 2) return new DeadlineDecision(learnerId, "DUE_SOON", days);
        return new DeadlineDecision(learnerId, "ON_TRACK", days);
    }

    public record DeadlineDecision(String learnerId, String status, long daysRemaining) { }
}
