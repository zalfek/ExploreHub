package models;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
public class Transactions {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private int Id;

    @Basic(optional=false)
    private Date Date;

    @Basic(optional=false)
    private int Completed;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="EventID", nullable=false)
    private Events event;

    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "StudentID", nullable=false)
    private User user;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        Date = date;
    }

    public int getCompleted() {
        return Completed;
    }

    public void setCompleted(int completed) {
        Completed = completed;
    }

    public Events getEvent() {
        return event;
    }

    public void setEvent(Events event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
