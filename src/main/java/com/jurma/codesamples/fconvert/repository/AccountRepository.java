package com.jurma.codesamples.fconvert.repository;

import com.jurma.codesamples.fconvert.entity.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    List<Account> findByClientId(Long id);
}
