package example;

import java.time.LocalDate;

public final class LearnerDeadlineServiceTest {
    public static void main(String[] args) {
        LearnerDeadlineService service = new LearnerDeadlineService(null);
        var decision = service.deadline("learner-7", LocalDate.of(2026, 9, 18), LocalDate.of(2026, 9, 16));
        if (!decision.status().equals("DUE_SOON") || decision.daysRemaining() != 2) {
            throw new AssertionError("deadline policy changed: " + decision);
        }
        System.out.println("deadline policy: DUE_SOON at two days");
    }
}
