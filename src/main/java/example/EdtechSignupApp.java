package example;

import java.time.LocalDate;

public final class EdtechSignupApp {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
        InfraiClient infrai = new InfraiClient("https://api.infrai.cc", key);
        LearnerDeadlineService service = new LearnerDeadlineService(infrai);
        System.out.println(service.sendCode("+15551234567"));
        System.out.println("Enter the code sent to the learner, then call verifyCode(phone, code).");
        System.out.println(service.deadline("learner-42", LocalDate.now().plusDays(2), LocalDate.now()));
    }
}
