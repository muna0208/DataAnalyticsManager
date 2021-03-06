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
     * ํ์ผ ์กฐํ
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
     * ํด๋ ์์ฑ
     * 
     * @param targetFullPath
     * @param fileName
     * @return
     */
    public ModelAndView createDirectory(String targetFullPath, String fileName) {
        ModelAndView mav = new ModelAndView("jsonView");

        if (FileUtil.fileIsLive(targetFullPath)) {
            logger.error("FAILED TO CREATE: ALREADY EXISTED FILE!!!");
            mav.addObject("error", "[" + fileName + "] ์ด๋ฏธ ์กด์ฌํ๋ ํ์ผ์๋๋ค.");
            return mav;
        }

        if (StringUtil.isNull(targetFullPath)) {
            logger.error("FAILED TO CREATE: NO FILE PATH!!!");
            mav.addObject("error", "ํ์ผ ๊ฒฝ๋ก๊ฐ ์์ต๋๋ค.");
            return mav;
        } else {
            FileUtil.makeFolder(targetFullPath);
            if (!FileUtil.fileIsLive(targetFullPath)) {
                logger.error("FAILED TO CREATE: TARGET FILE IS OPENED!!!");
                mav.addObject("error", "[" + fileName + "] ํ์ผ์์ฑ ์คํจ");
                return mav;
            }
        }

        mav.addObject("info", "[" + fileName + "]์์ฑ์ ์ฑ๊ณตํ์์ต๋๋ค.");
        return mav;
    }

    /**
     * ํ์ผ/ํด๋ ์ญ์?
     * 
     * @param multiSelectPath
     * @return
     */
    public ModelAndView deleteFileDirectory(String multiSelectPath) {
        ModelAndView mav = new ModelAndView("jsonView");

        if (StringUtil.isNull(multiSelectPath)) {
            logger.error("FAILED TO DELETE: NO FILE/FOLDER PATH!!!");
            mav.addObject("error", "์?ํ๋ ํ์ผ ๊ฒฝ๋ก๊ฐ ์์ต๋๋ค.");
            return mav;
        } else {
            String[] toks = multiSelectPath.split("###");

            // ํ์ผ์ ๋ฏธ๋ฆฌ๋ณด๊ธฐ ํ๋ ๊ณผ์?์์ inputStream์ ๋จ์์๋ค..
            // ํด๋น inputStream์ close ํ์ฌ๋ ์ง์์ง์ง ์์ GC ๋ฅผ์ด์ฉํ์ฌ ์์ฌ ์์ ์?๊ฑฐ ํ ์ญ์?
            System.gc();
            for (String path : toks) {
                File f = new File(path);
                if (f.isDirectory())
                    FileUtil.deleteAllFiles(path);
                f.delete();
            }
        }

        mav.addObject("info", "์ญ์?์ ์ฑ๊ณตํ์์ต๋๋ค.");
        return mav;
    }

    /**
     * ํ์ผ/ํด๋ ์ด๋
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
            mav.addObject("error", "Old Path๊ฐ ์?ํจํ์ง ์์ต๋๋ค.");
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
                mav.addObject("error", "์ด๋ํ? ์์น๋ฅผ ์ฌ๋ฐ๋ฅด๊ฒ ์ค์?ํด์ฃผ์ธ์.");
                return mav;
            }
            if (copyFileDirectory(oldFile, newFile)) {
                userMngService.deleteFolder(oldPath);
            } else {
                logger.error("WRONG File/Folder PATH!!!");
                mav.addObject("error", "์ด๋ํ? ์์น๋ฅผ ์ฌ๋ฐ๋ฅด๊ฒ ์?ํํด์ฃผ์ธ์.");
                return mav;
            }
        }
        mav.addObject("info", "์ด๋์ ์ฑ๊ณตํ์์ต๋๋ค.");
        return mav;
    }

    /**
     * ํ์ผ/ํด๋ ๋ณต์ฌ
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
     * ํ์ผ/ํด๋ ๋ณต์ฌ ๋ก์ง
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
     * ํ์ผ/ํด๋ ์ด๋ฆ ์์?
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
            mav.addObject("error", "ํ์ผ ๊ฒฝ๋ก๊ฐ ์์ต๋๋ค.");
            return mav;
        }
        targetPath = FileUtil.appendEndsPath(targetPath);

        if (StringUtil.isNull(oldName)) {
            mav.addObject("error", "์ด์? ํ์ผ/ํด๋ ์ด๋ฆ์ด ์์ต๋๋ค.");
            logger.error("FAILED TO MODIFY NAME: NO PARENT FILE/FOLDER NAME!!!");
            return mav;
        } else if (StringUtil.isNull(newName)) {
            logger.error("FAILED TO MODIFY NAME: NO NEW FILE/FOLDER NAME!!!");
            mav.addObject("error", "์๋ก์ด ํ์ผ/ํด๋ ์ด๋ฆ์ด ์์ต๋๋ค.");
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
                mav.addObject("error", "[" + newName + "]์ด๋ฏธ ์กด์ฌํ๋ ํ์ผ/ํด๋ ์๋๋ค");
                return mav;
            } else {
                if (file.exists())
                    file.renameTo(fileNew);
                else {
                    logger.error("FAILED TO MODIFY NAME: NOT EXISTED FILE/FOLDER!!!");
                    mav.addObject("error", "[" + oldName + "]์กด์ฌํ์ง ์์ ํ์ผ/ํด๋ ์๋๋ค");
                    return mav;
                }
            }
        }

        mav.addObject("info", "์์?์ ์ฑ๊ณตํ์์ต๋๋ค.");
        return mav;
    }

    /**
     * ๋ก์ปฌ ๋ฐ์ดํฐ ์๋ก๋
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
     * ์ํ๋ฐ์ดํฐ ๊ฐ์?ธ์ค๊ธฐ
     * 
     * @param fileFullPath
     * @param sample
     * @param fileName
     * @return
     * @throws Exception
     */
    public ModelAndView getSampleData(String fileFullPath, String sample, String fileName) throws Exception {
        ModelAndView mav = new ModelAndView("jsonView");

        // ์๋ฒ๊ฒฝ๋ก๋ฅผ ์ํ์ค๋ค
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
     * tPath์ ์๋ฒ๊ฒฝ๋ก ์ํ๊ธฐ
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
     * sPath ๊ฐ์?ธ์ค๊ธฐ
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
     * ์๋ฒํ์ผ์ ๋ฐ์ดํฐ ๊ธฐ๋ณธ์?๋ณด๋ฅผ ๊ฐ์?ธ์จ๋ค
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
     * ๋๋?ํ?๋ฆฌ ๊ฒฝ๋ก ์ฐพ๊ธฐ (๋ก์ปฌ์ผ ๋)
     * 
     * @param directoryPath
     * @return
     */
    public String getDirectoryPath(String directoryPath) {
        // ๋ก์ปฌํ์คํธ ๋ชจ๋์ธ๋ฐ
        if ("true".equalsIgnoreCase(isTest)) {
            directoryPath = directoryPath.replaceAll("\\\\", "/");
            fileTestPath = fileTestPath.replaceAll("\\\\", "/");
            filePath = filePath.replaceAll("\\\\", "/");
            sampleFileTestPath = sampleFileTestPath.replaceAll("\\\\", "/");
            sampleFilePath = sampleFilePath.replaceAll("\\\\", "/");

            if (directoryPath.contains(fileTestPath) || directoryPath.contains(sampleFileTestPath)) {
                // ๋ก์ปฌํ์คํธ ๋๋?ํ?๋ฆฌ๋ฅผ ํฌํจํ๊ณ?์์ผ๋ฉด ์๋ฌด์์์ ์ํจ
                // ์ด๋ฏธ์ง ๋ฏธ๋ฆฌ๋ณด๊ธฐ๊ฐ ๊ฐ๋ฅํ ๊ฒฝ๋ก์ด๋ค
            } else {
                // ์๋ฒ๋๋?ํ?๋ฆฌ๋ฅผ ํฌํจํ? ๊ฒฝ์ฐ.. ๋ก์ปฌํ์คํธ ๊ฒฝ๋ก์ ์ด๋ฏธ์ง๋ฅผ ํ๋ณดํ?์ ์์ด ๋ก์ปฌํ์คํธ ๊ฒฝ๋ก๋ก ๊ฐ์?๋ก ๋ฐ๊พธ์ด์ค๋ค.
                if (directoryPath.contains(filePath))
                    directoryPath = fileTestPath + directoryPath.replaceAll(filePath, "");
                else if (directoryPath.contains(sampleFilePath))
                    directoryPath = sampleFileTestPath + directoryPath.replaceAll(sampleFilePath, "");
            }
        }

        return directoryPath;
    }

    /**
     * DataLableMap ๊ฐ์?ธ์ค๊ธฐ
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
     * ImageList ๊ฐ์?ธ์ค๊ธฐ
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
     * DataInfoList ๊ฐ์?ธ์ค๊ธฐ
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
     * ๋๋ค์ซ์์ ๋ฒ์์ ๋๋ค์๋ฅผ ๊ฒฐ๊ณผ ์ฌ์ด์ฆ ๋งํผ ๋ฆฌ์คํธ๋ก ๋ง๋ค์ด ๋ฆฌ
     * 
     * @param randomSize ๋๋ค์ซ๋ฒ์
     * @param resultSize ๊ฒฐ๊ณผ์ฌ์ด์ฆ
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
     * ๋จ์ผ ์ด๋ฏธ์ง ํ์ผ ๋ถ๋ฌ์ค๊ธฐ
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
     * ์ด๋ฏธ์ง ํ์ผ๋ค์ ๊ฒฝ๋ก ๊ฐ์?ธ์ค๊ธฐ
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
     * user๋ณ ์ฉ๋ ์ฒดํฌ
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
     * ์ฉ๋์ฒดํฌ์ ๋ฆฌํด ๊ฐ์ผ๋ก ๋ณด๋ผ ๊ฐ์ฒด๋ฅผ ๊ตฌ์ฑ
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