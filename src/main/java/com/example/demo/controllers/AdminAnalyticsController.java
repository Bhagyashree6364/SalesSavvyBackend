package com.example.demo.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.services.OrderService;

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminAnalyticsController {

    private final OrderService orderService;

    public AdminAnalyticsController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDailyBusiness(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        BigDecimal total = orderService.calculateTotalForDate(date);
        long orders = orderService.countOrdersForDate(date);
        return ResponseEntity.ok(
                Map.of("date", date.toString(), "totalRevenue", total, "ordersCount", orders));
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMonthlyBusiness(
            @RequestParam("month") int month,
            @RequestParam("year") int year) {

        YearMonth ym = YearMonth.of(year, month);
        BigDecimal total = orderService.calculateTotalForMonth(ym);
        long orders = orderService.countOrdersForMonth(ym);
        return ResponseEntity.ok(
                Map.of("month", month, "year", year, "totalRevenue", total, "ordersCount", orders));
    }

    @GetMapping("/yearly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getYearlyBusiness(@RequestParam("year") int year) {
        Year y = Year.of(year);
        BigDecimal total = orderService.calculateTotalForYear(y);
        long orders = orderService.countOrdersForYear(y);
        return ResponseEntity.ok(
                Map.of("year", year, "totalRevenue", total, "ordersCount", orders));
    }

    @GetMapping("/overall")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOverallBusiness() {
        BigDecimal total = orderService.calculateTotalOverall();
        long orders = orderService.countOrdersOverall();
        return ResponseEntity.ok(
                Map.of("totalRevenue", total, "ordersCount", orders));
    }
}
