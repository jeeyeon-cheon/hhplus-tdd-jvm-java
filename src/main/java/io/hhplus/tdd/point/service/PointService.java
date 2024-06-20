package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.*;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ConcurrentHashMap<Long, Lock> lockMap = new ConcurrentHashMap<>();

    public UserPoint getPoint(long id) {
        if (id < 1) throw new UnsupportedOperationException();
        return userPointRepository.selectById(id);
    }
    public List<PointHistory> getHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }
    public UserPoint charge(long id, long amount) {
        Lock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
        lock.lock();
        try {
            UserPoint userPoint = userPointRepository.selectById(id);
            long chargedPoint = userPoint.point() + amount;
            UserPoint resPoint = userPointRepository.insertOrUpdate(id, chargedPoint);
            pointHistoryRepository.insert(id, amount, TransactionType.CHARGE, resPoint.updateMillis());
            return resPoint;
        } finally {
            lock.unlock();
        }
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
