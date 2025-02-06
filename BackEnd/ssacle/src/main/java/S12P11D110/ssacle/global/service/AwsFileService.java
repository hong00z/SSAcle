package S12P11D110.ssacle.global.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import com.amazonaws.services.s3.model.CannedAccessControlList;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j //AwsFileService에서 로그를 남기고 싶을 때 사용 되는 라이브러리
public class AwsFileService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private String PROFILE_IMG_DIR = "profile/"; // 유저 프로필
//    private String STUDY_IMG_DIR = "Study/"; // 스터디 프로필


    // 파일 업로드: 클라이언트가 업로드한 MultipartFile을 File로 변환
    public String savePhoto(MultipartFile multipartFile, String userId) throws IOException {
        File uploadFile = convert(multipartFile ) // 파일 변활 할 수 없으면 에러
                .orElseThrow(()->new IllegalArgumentException("error: MultipartFile -> File convert fail"));
        return upload(uploadFile, PROFILE_IMG_DIR, userId); //upload() 메서드를 이용해 AWS S3에 업로드
    }

    // convert 함수: MultipartFile → File 변환
    private Optional<File> convert(MultipartFile file){
        // 임시 디렉토리에 파일 생성
        File convertFile = new File(System.getProperty("java.io.tmpdir")+"/"+file.getOriginalFilename());

        // 파일을 byte[] 로 변환 후 저장
        try (FileOutputStream fos = new FileOutputStream(convertFile)) {
            fos.write(file.getBytes());
            return Optional.of(convertFile);
        } catch (IOException e) {
            return Optional.empty();
        }
    }


    // S3로 파일 업로드하기
    private String upload(File uploadfile, String dirName, String userId){
        // S3에 저장될 파일 이름 설정
        String fileName = dirName + userId + "/" + UUID.randomUUID() + uploadfile.getName(); //  UUID.randomUUID(): 중복방지용 고유 파일명 생성
        // S3에 업로드
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadfile)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        // 로컬 파일 삭제
        removeNewFile(uploadfile);

        // S3 URL 반환
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 업로드 후 로컬 파일 삭제
    private void removeNewFile(File targetFile){
        if(targetFile.delete()){
            log.info("로컬 파일 삭제 성공: {}", targetFile.getName());
            return;
        }
        log.warn("로컬 파일 삭제 실패: {}", targetFile.getName());
    }

}
