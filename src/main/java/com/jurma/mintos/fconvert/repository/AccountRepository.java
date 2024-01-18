package com.jurma.mintos.fconvert.repository;

import com.jurma.mintos.fconvert.entity.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    List<Account> findByClientId(Long id);
}
