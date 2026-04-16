package nl.zerofifty.springaiaccelerator.infrastructure.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("session")
public class Session {

    @Id
    private Long id;

    private String userId;
    private String chatId;

    public Session(String userId, String chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    public Session() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
