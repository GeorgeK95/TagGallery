package TagGallery.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import TagGallery.entity.User;

import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    @Query("select u.email from User u")
    Set<String> getAllEmails();
}