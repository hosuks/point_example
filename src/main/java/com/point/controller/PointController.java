package com.point.controller;

import com.point.dto.*;
import com.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/earn")
    public ResponseEntity<EarnResponse> earn(@Valid @RequestBody EarnRequest request) {
        return ResponseEntity.ok(pointService.earn(request));
    }

    @PostMapping("/earn/cancel")
    public ResponseEntity<CancelEarnResponse> cancelEarn(@Valid @RequestBody CancelEarnRequest request) {
        return ResponseEntity.ok(pointService.cancelEarn(request));
    }

    @PostMapping("/use")
    public ResponseEntity<UseResponse> use(@Valid @RequestBody UseRequest request) {
        return ResponseEntity.ok(pointService.use(request));
    }

    @PostMapping("/use/cancel")
    public ResponseEntity<CancelUseResponse> cancelUse(@Valid @RequestBody CancelUseRequest request) {
        return ResponseEntity.ok(pointService.cancelUse(request));
    }

    @GetMapping("/balance/{memberId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long memberId) {
        return ResponseEntity.ok(pointService.getBalanceDetail(memberId));
    }

    @GetMapping("/transactions/{memberId}")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable Long memberId) {
        return ResponseEntity.ok(pointService.getTransactionHistory(memberId));
    }
}
