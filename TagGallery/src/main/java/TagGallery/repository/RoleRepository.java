package TagGallery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import TagGallery.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}