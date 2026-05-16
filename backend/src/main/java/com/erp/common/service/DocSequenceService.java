package com.erp.common.service;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class DocSequenceService {

    private final DocSequenceRepository repository;

    public DocSequenceService(DocSequenceRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(Long companyId, String prefix) {
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = prefix + "-" + companyId + "-" + yearMonth;

        DocSequence seq = repository.findBySeqKeyForUpdate(key)
                .orElseGet(() -> repository.save(new DocSequence(key)));

        seq.increment();
        return prefix + "-" + yearMonth + "-" + String.format("%04d", seq.getLastSeq());
    }
}

@Entity
@Table(name = "doc_sequence")
@Getter
@NoArgsConstructor
class DocSequence {

    @Id
    @Column(length = 60)
    private String seqKey;

    private int lastSeq = 0;

    DocSequence(String seqKey) {
        this.seqKey = seqKey;
    }

    void increment() {
        this.lastSeq++;
    }
}

interface DocSequenceRepository extends JpaRepository<DocSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DocSequence d WHERE d.seqKey = :key")
    Optional<DocSequence> findBySeqKeyForUpdate(@Param("key") String key);
}
