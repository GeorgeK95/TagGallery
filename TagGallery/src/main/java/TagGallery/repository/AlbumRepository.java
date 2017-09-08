package TagGallery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import TagGallery.entity.Album;
import TagGallery.entity.User;

import java.util.List;

/**
 * Created by George-Lenovo on 6/28/2017.
 */
@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    @Query("select a from Album a where a.author=:userParam")
    List<Album> getUserAlbums(@Param("userParam") User user);
}
