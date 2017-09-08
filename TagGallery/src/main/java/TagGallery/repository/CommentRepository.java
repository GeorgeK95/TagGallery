package TagGallery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import TagGallery.entity.Comment;
import TagGallery.entity.Image;

import java.util.List;

/**
 * Created by George-Lenovo on 6/28/2017.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByImageHolderOrderByIdAsc(Image imageHolder);

}
