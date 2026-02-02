package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.domain.store.entity.Store;
import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Analysis extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(length = 255)
    private String catchphrase;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL)
    private List<Swot> swots = new ArrayList<>();

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL)
    private List<ActionPlan> actionPlans = new ArrayList<>();

    public void updateCatchphrase(String catchphrase) {
        this.catchphrase = catchphrase;
    }
}
