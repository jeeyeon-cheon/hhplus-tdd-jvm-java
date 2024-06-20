package io.hhplus.tdd.point;

import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PointServiceConcurrentTest {

    @Autowired
    PointService pointService;
    @Test
    @DisplayName("한 유저가 충전을 동시에 여러번 시도시, 충전 종료 후 해당 유저의 포인트와 충전 총 금액이 일치한다")
    public void 일단() throws InterruptedException {
        // Given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        // 100원 10번 충전
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    UserPoint userPoint  = pointService.charge(1, 100);
                    System.out.println(userPoint);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        // Then
        var point = pointService.getPoint(1);
        assertEquals(1000, point.point());
    }
}
