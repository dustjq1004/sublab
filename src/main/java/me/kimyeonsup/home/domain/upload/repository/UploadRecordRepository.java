package me.kimyeonsup.home.domain.upload.repository;


import java.util.List;
import me.kimyeonsup.home.domain.upload.domain.entity.UploadedImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadRecordRepository extends JpaRepository<UploadedImage, Long> {

    List<UploadedImage> findByUserId(String userId);
}
