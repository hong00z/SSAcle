package S12P11D110.ssacle.global.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.File;

@Service
//FileStorageService의 실제 구현체
public class FileStorageServiceImpl implements  FileStorageService{

    // 설정 파일(application.yml)에서 업로드 디렉토리 경로 가져오기
    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct //애플리케이션이 실행될 때 자동으로 업로드 디렉토리를 생성
    @Override
    public void init(){
        File directory = new File(uploadDir);
        if(!directory.exists()){
            directory.mkdirs(); // 디렉토리가 없으면 생성
        }
    }

    @Override
    // 저장된 파일이 들어갈 디렉토리 경로를 반환
    public String getUploadDir(){
        return uploadDir;
    }
}
