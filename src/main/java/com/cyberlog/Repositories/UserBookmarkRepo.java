package com.cyberlog.Repositories;

import com.cyberlog.Models.UserBookmark;
import com.cyberlog.Models.User;
import com.cyberlog.Models.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBookmarkRepo extends JpaRepository<UserBookmark, UUID> {
    List<UserBookmark> findByUser(User user);

    UserBookmark findByArticle(Article article);

    void deleteByUser(User user);
}
