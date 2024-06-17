package io.hhplus.tdd.point;

import io.hhplus.tdd.ErrorResponse;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointService pointService; //@Mock 객체를 주입받는 객체

    @Mock
    private UserPointRepository userPointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private PointHistoryTable pointHistoryTable;

    /* 작성이유 : 특정 유저의 포인트를 조회하는 기능을 확인하기 위함 */
    @Test
    @DisplayName("특정 유저의 포인트를 조회한다")
    public void 특정_유저의_포인트를_조회한다() {
        // Given : repository 유저 1조회시 0 포인트를 가진 UserPoint 객체 반환 가정
        long id = 1l;
        UserPoint point = new UserPoint(id, 0, System.currentTimeMillis());
        when(userPointRepository.selectById(id)).thenReturn(point);

        // When
        UserPoint userPoint = pointService.getPoint(id);

        // Then
        assertEquals(1l, userPoint.id());
        assertEquals(0, userPoint.point());
    }

    /* 작성이유 : 특정 유저의 포인트 충전 내역을 조회하는 기능을 확인하기 위함 */
    @Test
    @DisplayName("특정 유저의 포인트 충전 내역을 조회한다")
    public void 특정_유저의_포인트_충전_내역을_조회한다() {
        // Given : repository 유저 2 조회시 100 포인트를 가진 PointHistory 객체 반환 가정
        long userId = 2l;
        PointHistory point = new PointHistory(1l, userId, 100, TransactionType.USE, System.currentTimeMillis());
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(Collections.singletonList(point));

        // When
        List<PointHistory> pointHistory = pointService.getHistory(userId);

        // Then
        assertEquals(1l, pointHistory.get(0).id());
        assertEquals(2l, pointHistory.get(0).userId());
        assertEquals(100, pointHistory.get(0).amount());
        assertEquals(TransactionType.USE, pointHistory.get(0).type());
    }

    /* 작성이유 : 특정 유저의 포인트를 충전했을 때 포인트가 업데이트되는지 확인하기 위함 */
    @Test
    @DisplayName("특정 유저의 포인트를 충전한다")
    public void 특정_유저의_포인트를_충전한다() {
        // Given : repository 유저 3 조회, 업데이트 시 정해진 UserPoint 객체 반환 가정
        long userId = 3l;
        UserPoint zeroPoint = new UserPoint(userId, 0, System.currentTimeMillis());
        UserPoint chargedPoint = new UserPoint(userId, 300, System.currentTimeMillis());
        when(userPointRepository.selectById(userId)).thenReturn(zeroPoint);
        when(userPointRepository.insertOrUpdate(userId, 300)).thenReturn(chargedPoint);

        // When
        UserPoint userPoint = pointService.charge(3l, 300);

        // Then
        assertEquals(3l, userPoint.id());
        assertEquals(300, userPoint.point());
    }

    /* 작성이유 : 특정 유저의 포인트를 충전했을 때 충전내역에 해당 내용이 업데이트되는지 확인하기 위함 */
    @Test
    @DisplayName("특정 유저의 포인트를 충전하고 히스토리 조회한다")
    public void 특정_유저의_포인트를_충전하고_히스토리_조회한다() {
        // Given : repository 유저 3 조회, 업데이트 시 정해진 UserPoint 객체 반환 가정
        long userId = 3l;
        UserPoint zeroPoint = new UserPoint(userId, 0, System.currentTimeMillis());
        UserPoint chargedPoint = new UserPoint(userId, 300, System.currentTimeMillis());
        List<PointHistory> pointHistories = List.of(new PointHistory(1, userId, 300, TransactionType.CHARGE, chargedPoint.updateMillis()));
        when(userPointRepository.selectById(userId)).thenReturn(zeroPoint);
        when(userPointRepository.insertOrUpdate(userId, 300)).thenReturn(chargedPoint);
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(pointHistories);

        // When
        UserPoint userPoint = pointService.charge(3l, 300);
        var history = pointService.getHistory(3l);

        // Then
        assertEquals(3l, userPoint.id());
        assertEquals(300, userPoint.point());
        assertEquals(3l, history.get(0).userId());
        assertEquals(TransactionType.CHARGE, history.get(0).type());
        assertEquals(300, history.get(0).amount());
    }

    /* 작성이유 : 특정 유저의 포인트를 사용했을 때 충전내역에 해당 내용이 업데이트되는지 확인하기 위함 */
    @Test
    @DisplayName("특정 유저의 포인트를 사용하고 히스토리 조회한다")
    public void 특정_유저의_포인트를_사용하고_히스토리_조회한다() throws Exception {
        // Given : repository 유저 3 조회, 업데이트 시 정해진 UserPoint 객체 반환 가정
        long userId = 3l;
        UserPoint point = new UserPoint(userId, 400, System.currentTimeMillis());
        UserPoint usedPoint = new UserPoint(userId, 300, System.currentTimeMillis());
        List<PointHistory> pointHistories = List.of(new PointHistory(1, userId, 100, TransactionType.USE, usedPoint.updateMillis()));
        when(userPointRepository.selectById(userId)).thenReturn(point);
        when(userPointRepository.insertOrUpdate(userId, 300)).thenReturn(usedPoint);
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(pointHistories);

        // When
        UserPoint userPoint = pointService.use(3l, 100);
        var history = pointService.getHistory(3l);

        // Then
        assertEquals(3l, userPoint.id());
        assertEquals(300, userPoint.point());
        assertEquals(3l, history.get(0).userId());
        assertEquals(TransactionType.USE, history.get(0).type());
        assertEquals(100, history.get(0).amount());
    }


    /* 작성이유 : 잔여 포인트를 초과하는 포인트 양의 사용지 실패하는지 확인하기 위함 */
    @Test
    @DisplayName("잔여 포인트보다 큰 포인트의 사용은 실패한다")
    public void 잔여_포인트보다_큰_포인트의_사용은_실패한다() throws Exception {
        // Given : repository 유저 3 조회, 업데이트 시 정해진 UserPoint 객체 반환 가정
        long userId = 4l;
        UserPoint point = new UserPoint(userId, 800, System.currentTimeMillis());
        when(userPointRepository.selectById(userId)).thenReturn(point);

        // When Then
        assertThrows(Exception.class, () -> pointService.use(4l, 900));
    }
}
