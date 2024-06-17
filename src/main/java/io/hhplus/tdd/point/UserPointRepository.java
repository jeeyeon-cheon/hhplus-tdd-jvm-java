package io.hhplus.tdd.point;

import org.springframework.stereotype.Repository;

@Repository
public interface UserPointRepository {
    UserPoint selectById(long id);
    UserPoint insertOrUpdate(long id, long amount);
}
