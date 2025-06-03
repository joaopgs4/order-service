//OrderModel.java
package store.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import store.account.AccountOut;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
public class OrderModel {

    @Id
    @Column(name = "id_order")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "id_account")
    private String idAccount;

    @Column(name = "dt_date")
    private Date date;

    @Column(name = "db_total")
    private Double total;

    public OrderModel(Order o) {
        this.id = o.id();
        this.idAccount = o.account() != null ? o.account().id() : null;
        this.date = o.date();
        this.total = o.total();
    }

    public Order to() {
        return Order.builder()
                .id(id)
                .account(AccountOut.builder().id(idAccount).build())
                .items(new ArrayList<>())
                .date(date)
                .total(total)
                .build();
    }
}