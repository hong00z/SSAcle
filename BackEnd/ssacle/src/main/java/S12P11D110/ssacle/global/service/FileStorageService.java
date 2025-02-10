package S12P11D110.ssacle.global.service;

//파일 저장 기능을 담당
public interface FileStorageService {

    public void init(); // 파일 저장소 초기화

    public String getUploadDir(); // 업로드 디렉토리 경로 반환

}
