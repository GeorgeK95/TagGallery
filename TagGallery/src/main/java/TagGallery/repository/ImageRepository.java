package TagGallery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import TagGallery.entity.Image;

import java.util.List;
import java.util.Set;


public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("select t.images from Tag t where t.tagName in :tags")
    List<Image> findImagesByTags(@Param("tags") Set<String> tags);

    @Query("select i from Image i where i.id in :ids order by i.postDate desc")
    List<Image> findByIdsOrderedByAddDate(@Param("ids") Set<Long> ids);

    @Query("select t.images from Tag t where t.tagName = :tag")
    Set<Image> findImagesByTag(@Param("tag") String tagName);
}
