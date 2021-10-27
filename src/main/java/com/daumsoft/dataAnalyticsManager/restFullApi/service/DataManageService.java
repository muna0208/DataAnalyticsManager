package com.daumsoft.dataAnalyticsManager.restFullApi.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.daumsoft.dataAnalyticsManager.common.utils.FileUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.StringUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import net.sf.json.JSONObject;

@Service
public class DataManageService {
    Logger logger = LoggerFactory.getLogger(DataManageService.class);

    @Value("${filePath}")
    private String filePath;

    @Value("${fileTestPath}")
    private String fileTestPath;

    @Value("${isTest}")
    private String isTest;

    @Value("${sampleFilePath}")
    private String sampleFilePath;

    @Value("${sampleFileTestPath}")
    private String sampleFileTestPath;

    @Value("${userMaxSpace}")
    private String userMaxSpace;

    @Autowired
    UserMngService userMngService;

    @Autowired
    ProjectRestService projectRestService;

    /**
     * 파일 조회
     * 
     * @param userId
     * @param userAuth
     * @return
     * @throws Exception
     */
    public Map<String, Object> makeFileList(String userId, String userAuth) throws Exception {
        Map<String, Object> rootMap = new HashMap<String, Object>();
        String folderName = "admin".equals(userAuth) ? "root" : userId;
        filePath = FileUtil.appendEndsPath(filePath);
        fileTestPath = FileUtil.appendEndsPath(fileTestPath);

        String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;
        if (!"admin".equals(userAuth)) {
            fpath = fpath + "users/" + userId;
        }

        List<Map<String, Object>> fList = FileUtil.getDirFileListObject(fpath);
        FileUtil.makeFolder(fpath);

        rootMap.put("type", "folder");
        rootMap.put("children", fList);
        rootMap.put("text", folderName);
        rootMap.put("path", fpath);

        return rootMap;
    }

    /**
     * 폴더 생성
     * 
     * @param targetFullPath
     * @param fileName
     * @return
     */
    public ModelAndView createDirectory(String targetFullPath, String fileName) {
        ModelAndView mav = new ModelAndView("jsonView");

        if (FileUtil.fileIsLive(targetFullPath)) {
            logger.error("FAILED TO CREATE: ALREADY EXISTED FILE!!!");
            mav.addObject("error", "[" + fileName + "] 이미 존재하는 파일입니다.");
            return mav;
        }

        if (StringUtil.isNull(targetFullPath)) {
            logger.error("FAILED TO CREATE: NO FILE PATH!!!");
            mav.addObject("error", "파일 경로가 없습니다.");
            return mav;
        } else {
            FileUtil.makeFolder(targetFullPath);
            if (!FileUtil.fileIsLive(targetFullPath)) {
                logger.error("FAILED TO CREATE: TARGET FILE IS OPENED!!!");
                mav.addObject("error", "[" + fileName + "] 파일생성 실패");
                return mav;
            }
        }

        mav.addObject("info", "[" + fileName + "]생성에 성공하였습니다.");
        return mav;
    }

    /**
     * 파일/폴더 삭제
     * 
     * @param multiSelectPath
     * @return
     */
    public ModelAndView deleteFileDirectory(String multiSelectPath) {
        ModelAndView mav = new ModelAndView("jsonView");

        if (StringUtil.isNull(multiSelectPath)) {
            logger.error("FAILED TO DELETE: NO FILE/FOLDER PATH!!!");
            mav.addObject("error", "선택된 파일 경로가 없습니다.");
            return mav;
        } else {
            String[] toks = multiSelectPath.split("###");

            // 파일을 미리보기 하는 과정에서 inputStream에 남아있다..
            // 해당 inputStream을 close 하여도 지워지지 않아 GC 를이용하여 잉여 자원 제거 후 삭제
            System.gc();
            for (String path : toks) {
                File f = new File(path);
                if (f.isDirectory())
                    FileUtil.deleteAllFiles(path);
                f.delete();
            }
        }

        mav.addObject("info", "삭제에 성공하였습니다.");
        return mav;
    }

    /**
     * 파일/폴더 이동
     * 
     * @param oldPath
     * @param newPath
     * @param userId
     * @param userAuth
     * @return
     */
    public ModelAndView moveFileDirectory(String oldPath, String newPath, String userId, String userAuth) {
        ModelAndView mav = new ModelAndView("jsonView");

        if (StringUtil.isNull(oldPath)) {
            logger.error("FAILED TO MOVE: NO OLD PATH!!!");
            mav.addObject("error", "Old Path가 유효하지 않습니다.");
            return mav;
        } else {
            String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;
            StringBuilder path = new StringBuilder(fpath);
            if ("user".equals(userAuth)) {
                path.append("/users/" + userId);
            }
            path.append(newPath);

            File oldFile = new File(oldPath);
            File newFile = new File(path.toString());
            if (!newFile.isDirectory()) {
                mav.addObject("error", "이동할 위치를 올바르게 설정해주세요.");
                return mav;
            }
            if (copyFileDirectory(oldFile, newFile)) {
                userMngService.deleteFolder(oldPath);
            } else {
                logger.error("WRONG File/Folder PATH!!!");
                mav.addObject("error", "이동할 위치를 올바르게 선택해주세요.");
                return mav;
            }
        }
        mav.addObject("info", "이동에 성공하였습니다.");
        return mav;
    }

    /**
     * 파일/폴더 복사
     * 
     * @param sourceF
     * @param targetF
     */
    public boolean copyFileDirectory(File sourceF, File targetF) {
        String path = targetF.getPath() + File.separator + sourceF.getName();
        if (path.equals(sourceF.getPath())) {
            return false;
        }

        File temp = new File(path);
        if (!sourceF.isDirectory()) {
            writeFileDirectory(sourceF, temp);
            return true;
        }

        temp.mkdir();
        File[] fileList = sourceF.listFiles();

        for (File file : fileList) {
            File newFile = new File(temp.getPath() + File.separator + file.getName());

            if (file.isDirectory()) {
                newFile.mkdir();
                copyFileDirectory(file, temp);
            } else {
                writeFileDirectory(file, newFile);
            }
        }
        return true;
    }

    /**
     * 파일/폴더 복사 로직
     * 
     * @param oldFile
     * @param newFile
     */
    public void writeFileDirectory(File oldFile, File newFile) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(oldFile);
            fos = new FileOutputStream(newFile);

            byte[] b = new byte[4096];
            int count = 0;

            while ((count = fis.read(b)) != -1) {
                fos.write(b, 0, count);
            }

        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "moveDirectory");
        } finally {
            try {
                fis.close();
                fos.close();
            } catch (Exception e) {
                MakeUtil.printErrorLogger(e, "closeFileStream");
            }
        }
    }

    /**
     * 파일/폴더 이름 수정
     * 
     * @param targetPath
     * @param targetParentPath
     * @param targetType
     * @param oldName
     * @param newName
     * @return
     */
    public ModelAndView renameFileDirectory(String targetPath, String targetParentPath, String targetType,
            String oldName, String newName) {
        ModelAndView mav = new ModelAndView("jsonView");

        if (StringUtil.isNull(targetPath)) {
            logger.error("FAILED TO MODIFY NAME: NO FILE/FOLDER PATH!!!");
            mav.addObject("error", "파일 경로가 없습니다.");
            return mav;
        }
        targetPath = FileUtil.appendEndsPath(targetPath);

        if (StringUtil.isNull(oldName)) {
            mav.addObject("error", "이전 파일/폴더 이름이 없습니다.");
            logger.error("FAILED TO MODIFY NAME: NO PARENT FILE/FOLDER NAME!!!");
            return mav;
        } else if (StringUtil.isNull(newName)) {
            logger.error("FAILED TO MODIFY NAME: NO NEW FILE/FOLDER NAME!!!");
            mav.addObject("error", "새로운 파일/폴더 이름이 없습니다.");
            return mav;
        } else if (newName.equals(oldName)) {
            return mav;
        } else {
            String oldPath = ("file".equals(targetType)) ? targetPath + oldName : targetParentPath + oldName;
            String newPath = ("file".equals(targetType)) ? targetPath + newName : targetParentPath + newName;

            File file = new File(oldPath);
            File fileNew = new File(newPath);
            if (fileNew.exists()) {
                logger.error("FAILED TO MODIFY NAME: ALREADY EXISTED FILE/FOLDER!!!");
                mav.addObject("error", "[" + newName + "]이미 존재하는 파일/폴더 입니다");
                return mav;
            } else {
                if (file.exists())
                    file.renameTo(fileNew);
                else {
                    logger.error("FAILED TO MODIFY NAME: NOT EXISTED FILE/FOLDER!!!");
                    mav.addObject("error", "[" + oldName + "]존재하지 않은 파일/폴더 입니다");
                    return mav;
                }
            }
        }

        mav.addObject("info", "수정에 성공하였습니다.");
        return mav;
    }

    /**
     * 로컬 데이터 업로드
     * 
     * @param multipartFile
     * @param targetFile
     * @throws IOException
     */
    public void localDataUpload(MultipartFile multipartFile, File targetFile) throws IOException {
        InputStream fileStream = multipartFile.getInputStream();
        FileUtils.copyInputStreamToFile(fileStream, targetFile);
    }

    /**
     * 샘플데이터 가져오기
     * 
     * @param fileFullPath
     * @param sample
     * @param fileName
     * @return
     * @throws Exception
     */
    public ModelAndView getSampleData(String fileFullPath, String sample, String fileName) throws Exception {
        ModelAndView mav = new ModelAndView("jsonView");

        // 서버경로를 입혀준다
        String tPath = getTPath(fileFullPath, sample);
        MakeUtil.log("sampleFilePath: " + sampleFilePath);

        String sPath = getSPath(fileFullPath, sample);
        MakeUtil.log("file path: " + tPath);

        List<Object> dataInfoList = getDataInfo(tPath, fileName);
        mav.addObject("dataInfo", dataInfoList);

        JSONObject result = projectRestService.instancesLocalFileSample(sPath);
        mav.addObject("result", result);

        return mav;
    }

    /**
     * tPath에 서버경로 입히기
     * 
     * @param fileFullPath
     * @param sample
     * @return
     */
    private String getTPath(String fileFullPath, String sample) {
        String tPath = fileFullPath;
        if ("true".equalsIgnoreCase(isTest)) {
            if (!"true".equalsIgnoreCase(sample)) {
                tPath = fileTestPath + fileFullPath;
            }
        } else {
            if (!"true".equalsIgnoreCase(sample)) {
                tPath = filePath + fileFullPath;
            } else {
                String[] tempPath = fileFullPath.split("/daSample/");
                tPath = sampleFilePath + tempPath[1];
            }
        }

        return tPath;
    }

    /**
     * sPath 가져오기
     * 
     * @param fileFullPath
     * @param sample
     * @return
     */
    private String getSPath(String fileFullPath, String sample) {
        String sPath = filePath + fileFullPath;
        if ("true".equalsIgnoreCase(sample)) {
            String[] tempPath = fileFullPath.split("/daSample/");
            sPath = sampleFilePath + tempPath[1];
        }

        return sPath;
    }

    /**
     * 서버파일의 데이터 기본정보를 가져온다
     * 
     * @param tPath
     * @param fileName
     * @return
     */
    public List<Object> getDataInfo(String tPath, String fileName) {
        List<Object> dataInfoList = new ArrayList<Object>();
        File f = new File(tPath);
        String FileType = FileUtil.getFileCategory(fileName);
        long fileSize = 0;
        String pattern = "yyyy-MM-dd hh:mm aa";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date lastModifiedDate = new Date(f.lastModified());

        if (FileUtil.fileIsLive(tPath)) {
            fileSize = f.length();
        }
        dataInfoList.add(fileName);
        dataInfoList.add(FileType);
        dataInfoList.add(FileUtil.getFormatFilesize(fileSize));
        dataInfoList.add(simpleDateFormat.format(lastModifiedDate));

        return dataInfoList;
    }

    /**
     * 디렉토리 경로 찾기 (로컬일 때)
     * 
     * @param directoryPath
     * @return
     */
    public String getDirectoryPath(String directoryPath) {
        // 로컬테스트 모듈인데
        if ("true".equalsIgnoreCase(isTest)) {
            directoryPath = directoryPath.replaceAll("\\\\", "/");
            fileTestPath = fileTestPath.replaceAll("\\\\", "/");
            filePath = filePath.replaceAll("\\\\", "/");
            sampleFileTestPath = sampleFileTestPath.replaceAll("\\\\", "/");
            sampleFilePath = sampleFilePath.replaceAll("\\\\", "/");

            if (directoryPath.contains(fileTestPath) || directoryPath.contains(sampleFileTestPath)) {
                // 로컬테스트 디렉토리를 포함하고있으면 아무작업을 안함
                // 이미지 미리보기가 가능한 경로이다
            } else {
                // 서버디렉토리를 포함할 경우.. 로컬테스트 경로의 이미지를 확보할수 없어 로컬테스트 경로로 강제로 바꾸어준다.
                if (directoryPath.contains(filePath))
                    directoryPath = fileTestPath + directoryPath.replaceAll(filePath, "");
                else if (directoryPath.contains(sampleFilePath))
                    directoryPath = sampleFileTestPath + directoryPath.replaceAll(sampleFilePath, "");
            }
        }

        return directoryPath;
    }

    /**
     * DataLableMap 가져오기
     * 
     * @param directoryPath
     * @return
     * @throws IOException
     */
    public Map<String, Integer> getDataLableMap(String directoryPath) throws IOException {
        Map<String, Integer> dataLableMap = new HashMap<String, Integer>();
        List<File> imageList = getImageList(directoryPath);

        MakeUtil.log("imageList: " + imageList);
        for (File f : imageList) {
            File ff = new File(f.getParent());
            if (dataLableMap.containsKey(ff.getName())) {
                int value = dataLableMap.get(ff.getName());
                dataLableMap.put(ff.getName(), ++value);
            } else {
                dataLableMap.put(ff.getName(), 1);
            }
        }

        return dataLableMap;
    }

    /**
     * ImageList 가져오기
     * 
     * @param directoryPath
     * @return
     * @throws IOException
     */
    private List<File> getImageList(String directoryPath) throws IOException {
        List<File> list = FileUtil.findFiles(directoryPath, "*", true);
        List<File> imageList = new ArrayList<File>();
        for (File f : list) {
            if (f.isDirectory())
                continue;
            String fName = f.getName();
            if (fName.contains(".jpg") || fName.contains(".png") || fName.contains(".gif") || fName.contains(".bmp")) {
                imageList.add(f);
            }
        }

        return imageList;
    }

    /**
     * DataInfoList 가져오기
     * 
     * @param directoryPath
     * @return
     * @throws IOException
     */
    public List<Map<String, Object>> getDataInfoList(String directoryPath) throws IOException {
        List<Map<String, Object>> dataInfoList = new ArrayList<Map<String, Object>>();
        List<File> imageList = getImageList(directoryPath);

        List<Integer> randomIdxList = getRandNumList(imageList.size(), 10);
        for (int idx : randomIdxList) {
            File f = imageList.get(idx);
            String FileType = FileUtil.getFileCategory(f.getName());
            String pattern = "yyyy-MM-dd hh:mm aa";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            Date lastModifiedDate = new Date(f.lastModified());

            Map<String, Object> dataInfoMap = new HashMap<String, Object>();
            dataInfoMap.put("name", f.getName());
            dataInfoMap.put("type", FileType);
            dataInfoMap.put("size", FileUtil.getFormatFilesize(f.length()));
            dataInfoMap.put("date", simpleDateFormat.format(lastModifiedDate));
            dataInfoMap.put("dir", f.getPath().replaceAll("\\\\", "/"));
            dataInfoList.add(dataInfoMap);
        }

        return dataInfoList;
    }

    /**
     * 랜덤숫자의 범위의 랜덤수를 결과 사이즈 만큼 리스트로 만들어 리
     * 
     * @param randomSize 랜덤숫범위
     * @param resultSize 결과사이즈
     * @return
     */
    private List<Integer> getRandNumList(int randomSize, int resultSize) {
        List<Integer> resultList = new ArrayList<Integer>();
        Set<Integer> set = new HashSet<Integer>();
        Random rand = new Random();
        while (resultList.size() < randomSize && resultList.size() < resultSize) {
            int iValue = rand.nextInt(randomSize); // 0 <= iValue < 10
            if (!set.contains(iValue)) {
                set.add(iValue);
                resultList.add(iValue);
            }
        }
        return resultList;
    }

    /**
     * 단일 이미지 파일 불러오기
     * 
     * @param fileFullPath
     * @return
     * @throws IOException
     */
    public byte[] displayFile(String fileFullPath) throws IOException {
        InputStream in = null;
        in = new FileInputStream(fileFullPath);
        byte[] entity = IOUtils.toByteArray(in);
        in.close();

        return entity;
    }

    /**
     * 이미지 파일들의 경로 가져오기
     * 
     * @param tPath
     * @return
     * @throws IOException
     */
    public Map<String, String> getImageMap(String tPath) throws IOException {
        List<File> list = FileUtil.findFiles(tPath, "*", true);
        Map<String, String> imageMap = new HashMap<String, String>();
        for (File f : list) {
            imageMap.put(f.getName(), f.getAbsolutePath().replaceAll("\\\\", "/"));
        }

        return imageMap;
    }

    /**
     * user별 용량 체크
     * 
     * @param fpath
     * @param userId
     * @param userAuth
     * @return
     * @throws IOException
     */
    public ModelAndView checkFullUserSpace(String fpath, String userId, String userAuth) throws IOException {
        final long FORMAT = 1024;
        long maxSize;
        StringBuilder path = new StringBuilder(fpath);

        if ("admin".equals(userAuth)) {
            maxSize = FORMAT * FORMAT * FORMAT * FORMAT;
            // File f = new File(path.toString());
            // mav.addObject("maxSize", FileUtil.getFormatFilesize(f.getTotalSpace()));
        } else {
            maxSize = Long.parseLong(userMaxSpace.replace("GB", "")) * FORMAT * FORMAT * FORMAT;
            path.append("users/").append(userId);
        }

        return getMav(path.toString(), maxSize, userAuth);
    }

    /**
     * 용량체크의 리턴 값으로 보낼 객체를 구성
     * 
     * @param path
     * @param totalSize
     * @param maxSize
     * @return
     * @throws IOException
     */
    private ModelAndView getMav(String path, long maxSize, String userAuth) throws IOException {
        ModelAndView mav = new ModelAndView("jsonView");
        long totalSize = 0;
        List<File> list = FileUtil.findFiles(path, "*", true);
        for (File ff : list) {
            totalSize += ff.length();
        }

        if (maxSize < totalSize) {
            mav.addObject("result", true);
        } else {
            mav.addObject("result", false);
        }
        mav.addObject("size", FileUtil.getFormatFilesize(totalSize));
        if ("admin".equals(userAuth)) {
            mav.addObject("maxSize", "1TB");
        } else {
            mav.addObject("maxSize", userMaxSpace);
        }

        return mav;
    }
}