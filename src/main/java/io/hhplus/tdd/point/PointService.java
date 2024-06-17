package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public UserPoint getPoint(long id) {
        if (id < 1) throw new UnsupportedOperationException();
        return userPointRepository.selectById(id);
    }
    public List<PointHistory> getHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }
    public UserPoint charge(long id, long amount) {
        UserPoint userPoint = userPointRepository.selectById(id);
        long chargedPoint = userPoint.point() + amount;
        UserPoint resPoint = userPointRepository.insertOrUpdate(id, chargedPoint);
        pointHistoryRepository.insert(id, amount, TransactionType.CHARGE, resPoint.updateMillis());
        return resPoint;
    }
    public UserPoint use(long id, long amount) throws Exception {
        UserPoint userPoint = userPointRepository.selectById(id);
        long leftPoint = userPoint.point() - amount;
        if (leftPoint < 0) {
            throw new Exception("잔여 포인트가 부족합니다");
        }
        UserPoint resPoint = userPointRepository.insertOrUpdate(id, leftPoint);
        pointHistoryRepository.insert(id, amount, TransactionType.USE, resPoint.updateMillis());
        return resPoint;
    }
}
