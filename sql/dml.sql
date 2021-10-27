-- ALGORITHM 테이블 --

-- 목록 조회
SELECT 
    * 
FROM 
    "ALGORITHM"
<where>
    <choose>
        <when test="option == 'Usage' and value != null">
            "LIBRARY_FUNCTION_DESCRIPTION" ILIKE '%'||#{value}||'%'
        </when>
        <when test="option == 'Algorithm' and value != null">
            "ALGORITHM_NAME" ILIKE '%'||#{value}||'%'
        </when>
        <when test="option == 'Library' and value != null">
            "LIBRARY_NAME" ILIKE '%'||#{value}||'%'
        </when>
        <when test="option == 'Data Type' and value != null">
            "SUPPORT_DATA_TYPE" ILIKE '%'||#{value}||'%'
        </when>
        <when test="option == 'all' and value != null">
            ("LIBRARY_FUNCTION_DESCRIPTION" ILIKE '%'||#{value}||'%'
            OR "ALGORITHM_NAME" ILIKE '%'||#{value}||'%'
            OR "LIBRARY_NAME" ILIKE '%'||#{value}||'%'
            OR "SUPPORT_DATA_TYPE" ILIKE '%'||#{value}||'%')
        </when>
    </choose>
    AND "USE_FLAG" = TRUE
</where>
ORDER BY "CREATE_DATETIME" DESC
LIMIT
    10
OFFSET
    #{pageNo};

-- 개별 조회
SELECT 
    * 
from 
    "ALGORITHM" 
where 
    "ALGORITHM_SEQUENCE_PK" = #{id}

-- 검색
SELECT
    * 
FROM 
    "ALGORITHM"
<if test="searchValue != null and searchValue != 'null' and searchValue != ''">
    WHERE "ALGORITHM_NAME" ILIKE concat('%', #{searchValue}, '%') 
    OR "LIBRARY_FUNCTION_USAGE" ILIKE concat('%', #{searchValue}, '%')
</if>
ORDER BY 
    "ALGORITHM_NAME"

-- 개수 조회
SELECT 
    COUNT("ALGORITHM_SEQUENCE_PK")
FROM
    "ALGORITHM"
<where>
    <choose>
        <when test="option == 'Usage' and value != null">
                "LIBRARY_FUNCTION_DESCRIPTION" ILIKE '%'||#{value}||'%'
            </when>
        <when test="option == 'Algorithm' and value != null">
                "ALGORITHM_NAME" ILIKE '%'||#{value}||'%'
            </when>
        <when test="option == 'Library' and value != null">
                "LIBRARY_NAME" ILIKE '%'||#{value}||'%'
            </when>
        <when test="option == 'Data Type' and value != null">
                "SUPPORT_DATA_TYPE" ILIKE '%'||#{value}||'%'
            </when>
        <when test="option == 'all' and value != null">
                ("LIBRARY_FUNCTION_DESCRIPTION" ILIKE '%'||#{value}||'%'
                OR "ALGORITHM_NAME" ILIKE '%'||#{value}||'%'
                OR "LIBRARY_NAME" ILIKE '%'||#{value}||'%'
                OR "SUPPORT_DATA_TYPE" ILIKE '%'||#{value}||'%')
            </when>
    </choose>
    AND "USE_FLAG" = TRUE
</where>

-- CATEGORY 테이블 --

-- 로우 생성
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('마케팅','광고 데이터 및 트랜드 데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('금융&경제','주택 데이터 및 주가데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('환경&에너지','미세먼지데이터, 기상데이터, iris데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('범죄','사기데이터, 사건데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('문화 ','영화, 게임, 스포츠 데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('정보통신','ADFA-LD데이터, 로그데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('건강','질병데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('제조','제조설비 이상감지 데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('정치','국회의원 언급량 등 데이터');
INSERT INTO public."CATEGORY"("CATEGORY_NAME","DESCRIPTION") VALUES ('기타','카테고리에 정의되지 않은 기타 데이터');

-- DOMAIN 테이블 --

-- 등록
INSERT INTO
    "DOMAIN" ( 
        "CATEGORY_NAME"
        , "USER_ID"
        , "TITLE"
        , "MODEL_SEQUENCE_PK"
        , "PROJECT_SEQUENCE_PK"
        , "REGT_DATE"
        , "REGT_USER"
        , "DESCRIPTION"
        , "UPDATE_DATE"
    ) VALUES ( 
        #{categoryName} 
        , #{userId}
        , #{title} 
        , #{modelSequencePk} 
        , #{projectSequencePk}
        , NOW()
        , #{regtUser}
        , #{description}
        , NOW()
    )

-- 목록 조회 & 검색
SELECT 
    "DOMAIN_ID"
    , "USER_ID"
    , "CATEGORY_NAME"
    , "TITLE"
    , "MODEL_SEQUENCE_PK"
    , "PROJECT_SEQUENCE_PK"
    , to_char("REGT_DATE", 'YYYY-MM-DD HH24:MI:SS') AS "REGT_DATE"
    , "REGT_USER"
    , "DESCRIPTION"
FROM 
    "DOMAIN"
<where>
    <choose>
        <when test="option == 'mine'">
            AND "USER_ID" = #{currentId}
        </when>
        <when test="option != '전체' and option != null">
                AND "CATEGORY_NAME" = #{option}
        </when>
    </choose>
    <if test="value != null and value != ''">
        AND ("TITLE" LIKE '%'||#{value}||'%'
            OR "REGT_USER" LIKE '%'||#{value}||'%')
    </if>
    <if test="startDate != null and endDate != null">
        AND <![CDATA[
                ("REGT_DATE" >= #{startDate}
            AND 
                "REGT_DATE" <= #{endDate})
        ]]>
    </if>
    AND "DELETE_FLAG" = FALSE
</where>
ORDER BY
    "REGT_DATE" 
DESC
LIMIT 
    10
OFFSET 
    #{pageNo}

-- 도메인 개수 조회
SELECT 
    COUNT("DOMAIN_ID")
FROM
    "DOMAIN"
<where>
    <choose>
        <when test="option == 'mine'">
            AND "USER_ID" = #{currentId}
        </when>
        <when test="option != '전체' and option != null">
            AND "CATEGORY_NAME" = #{option}
        </when>
    </choose>
    <if test="value != null and value != ''">
        AND ("TITLE" LIKE '%'||#{value}||'%'
            OR "REGT_USER" LIKE '%'||#{value}||'%')
    </if>
    <if test="startDate != null and endDate != null">
        AND <![CDATA[
                ("REGT_DATE" >= #{startDate}
            AND 
                "REGT_DATE" <= #{endDate})
        ]]>
    </if>
    AND "DELETE_FLAG" = FALSE
</where>

-- 개별 조회
SELECT 
    "DOMAIN_ID"
    , "USER_ID"
    , "CATEGORY_NAME"
    , "TITLE"
    , "MODEL_SEQUENCE_PK"
    , "PROJECT_SEQUENCE_PK"
    , to_char("REGT_DATE", 'YYYY-MM-DD') AS "REGT_DATE"
    , "REGT_USER"
    , "DESCRIPTION"
FROM 
    "DOMAIN"
WHERE
    "DOMAIN_ID" = #{domainId}
AND 
    "DELETE_FLAG" = FALSE

-- 삭제
DELETE
FROM
    "DOMAIN"
WHERE
<foreach collection="list" item="item" index="index" separator="OR">
        "DOMAIN_ID" = #{item}
</foreach>

-- 수정
UPDATE
    "DOMAIN"
SET
    "CATEGORY_NAME" = #{categoryName}
    , "TITLE" = #{title}
    , "MODEL_SEQUENCE_PK" = #{modelSequencePk}
    , "PROJECT_SEQUENCE_PK" = #{projectSequencePk}
    , "DESCRIPTION" = #{description}
    , "UPDATE_DATE" = NOW()
WHERE
    "DOMAIN_ID" = #{domainId}
AND
    "DELETE_FLAG" = FALSE

-- 최근 만들어진 도메인 조회 (대시보드)
SELECT
    "DOMAIN_ID"
    , "MODEL_SEQUENCE_PK"
    , "PROJECT_SEQUENCE_PK"
    , "TITLE"
FROM
    "DOMAIN"
WHERE
    "DELETE_FLAG" = FALSE
ORDER BY
    "REGT_DATE" 
DESC
LIMIT 
    9

-- 다운로드
SELECT
    DATA."FILEPATH"
FROM
    <choose>
        <when test="type == 'model'">
            "MODEL"
        </when>
        <when test="type == 'original_data'">
            "ORIGINAL_DATA"
        </when>
        <when test="type == 'preprocessed_data'">
            "PREPROCESSED_DATA"
        </when>
    </choose>
    AS DATA
    <if test="type != 'model'">
        JOIN
            "MODEL" AS M
        ON
    </if>
    <choose>
        <when test="type == 'original_data'">
            DATA."ORIGINAL_DATA_SEQUENCE_PK" = M."ORIGINAL_DATA_SEQUENCE_FK2"
        </when>
        <when test="type == 'preprocessed_data'">
            DATA."PREPROCESSED_DATA_SEQUENCE_PK" = M."PREPROCESSED_DATA_SEQUENCE_FK3"
        </when>
    </choose>
    JOIN
        "DOMAIN" AS D
    ON
        D."MODEL_SEQUENCE_PK" = 
        <choose>
            <when test="type == 'model'">
                DATA
            </when>
            <otherwise>
                M
            </otherwise>
        </choose>
        ."MODEL_SEQUENCE_PK"
    AND
        D."DOMAIN_ID" = #{domainId}

-- PROJECT 테이블 --

-- 등록
INSERT INTO 
    "PROJECT"(
        "NAME"
        , "DESCRIPTION"
        , "CREATE_DATETIME"
        , "USER_ID"
    )VALUES(
        #{name}
        , #{description}
        , NOW()
        , #{userId}
    )

-- 목록 조회
SELECT 
    *
    , to_char("CREATE_DATETIME", 'YYYY-MM-DD') as "createDataTime"
FROM "PROJECT" AS project
<where>
    <choose>
        <when test="option == 'name' and value != null">
                    "NAME" 
                ILIKE 
                    '%'||#{value}||'%'
            </when>
        <when test="option == 'description' and value != null">
                    "DESCRIPTION" 
                ILIKE 
                    '%'||#{value}||'%'
            </when>
        <when test="option == 'regtUser' and value != null">
                "USER_ID" 
            ILIKE 
                '%'||#{value}||'%'
        </when>
        <when test="option == 'create_datetime' and value != null">
                    to_char("CREATE_DATETIME", 'YYYY-MM-DD') 
                ILIKE 
                    '%'||#{value}||'%'
            </when>
        <when test="option == 'all' and value != null">
            ("NAME" ILIKE '%'||#{value}||'%'
                OR "DESCRIPTION" ILIKE '%'||#{value}||'%'
                OR to_char("CREATE_DATETIME", 'YYYY-MM-DD') ILIKE '%'||#{value}||'%'
            <if test="currentId == ''">
                OR "USER_ID" ILIKE '%'||#{value}||'%'
            </if>
            )
        </when>
    </choose>
    <if test="currentId != null and currentId != 'null' and currentId != ''">
                AND "USER_ID" = #{currentId}
            </if>
    AND "DELETE_FLAG" = false
</where>
ORDER BY "CREATE_DATETIME" DESC
LIMIT
    10
OFFSET
    #{pageNo}

-- 프로젝트 이름 조회
SELECT
    "PROJECT_SEQUENCE_PK"
    , "NAME"
FROM
    "PROJECT"
WHERE
    "DELETE_FLAG" = FALSE
<if test="userAuth == 'user'">
        AND "USER_ID" = #{currentId}
</if>

-- 프로젝트 개별 조회
SELECT 
    *
    , to_char("CREATE_DATETIME", 'YYYY-MM-DD') as "createDataTime"
FROM "PROJECT" AS project
WHERE "PROJECT_SEQUENCE_PK" = #{projectSequencePk}

-- 프로젝트 개수 조회
SELECT 
    COUNT("PROJECT_SEQUENCE_PK") 
FROM 
    "PROJECT"
<where>
    <choose>
        <when test="option == 'name' and value != null">
                    "NAME" 
                ILIKE 
                    '%'||#{value}||'%'
            </when>
        <when test="option == 'description' and value != null">
                    "DESCRIPTION" 
                ILIKE 
                    '%'||#{value}||'%'
            </when>
        <when test="option == 'regtUser' and value != null">
                "USER_ID" 
            ILIKE 
                '%'||#{value}||'%'
        </when>
        <when test="option == 'create_datetime' and value != null">
                    to_char("CREATE_DATETIME", 'YYYY-MM-DD')
                ILIKE 
                    '%'||#{value}||'%'
            </when>
        <when test="option == 'all' and value != null">
            ("NAME" ILIKE '%'||#{value}||'%'
                OR "DESCRIPTION" ILIKE '%'||#{value}||'%'
                OR to_char("CREATE_DATETIME", 'YYYY-MM-DD') ILIKE '%'||#{value}||'%'
            <if test="currentId == ''">
                OR "USER_ID" ILIKE '%'||#{value}||'%'
            </if>
            )
        </when>
    </choose>
    <if test="currentId != null and currentId != 'null' and currentId != ''">
                AND "USER_ID" = #{currentId}
            </if>
    AND "DELETE_FLAG" = false
</where>

-- 프로젝트 수정
UPDATE 
    "PROJECT"
SET
<trim prefixOverrides=",">
    <if test="name != null and name != ''">,"NAME" = #{name}</if>
    <if test="description != null and description != ''">,"DESCRIPTION" = #{description}</if>
    <if test="deleteFlag != null and deleteFlag != ''">,"DELETE_FLAG" = #{deleteFlag}</if>
</trim>
WHERE 
    "PROJECT_SEQUENCE_PK" = #{projectSequencePk}

-- USER 테이블 --

-- 등록
INSERT INTO 
    "USER"(
        "USER_ID"
        , "USER_NAME"
        , "EMAIL"
        , "PW"
        , "INSERT_DATE"
        , "UPDATE_DATE"
        , "INSERT_NAME"
        , "UPDATE_NAME"
        , "USER_AUTH"
        , "USE_FLAG"
        , "LAST_ACCESS_DATE"
        , "PHONE_NUMBER"
    ) VALUES (
        #{user.userId}
        , #{user.userName}
        , #{user.email}
        , #{user.password}
        , NOW()
        , NOW()
<choose>
    <when test="currentName != null">
        , #{currentName}
        , #{currentName}
    </when>
    <otherwise>
        , #{user.userName}
        , #{user.userName}
    </otherwise>
</choose>
<choose>
    <when test="user.userAuth != null">
        , #{user.userAuth}
        , #{user.useFlag}
    </when>
    <otherwise>
        , 'user'
        , true
    </otherwise>
</choose>
, NOW()
<choose>
    <when test="user.phoneNumber == null">
        , ''
    </when>
    <otherwise>
        , #{user.phoneNumber}
    </otherwise>
</choose>
)

-- 목록 조회
SELECT
    "PK", "USER_ID", "USER_NAME", "EMAIL", "USE_FLAG", "INSERT_NAME", "UPDATE_NAME", "USER_AUTH"
    , COALESCE(to_char("LAST_ACCESS_DATE", 'YYYY-MM-DD HH24:MI:SS'), '-') AS "lastAccessDate" 
    , COALESCE(to_char("UPDATE_DATE", 'YYYY-MM-DD HH24:MI:SS'), '') AS "updateDate"
    , to_char("INSERT_DATE", 'YYYY-MM-DD HH24:MI:SS') AS "insertDate"
FROM 
    "USER" AS u
<where>
    <choose>
        <when test="option == 'userName' and value != null">
                "USER_NAME" 
            ILIKE 
                '%'||#{value}||'%'
        </when>
        <when test="option == 'userId' and value != null">
                "USER_ID" 
            ILIKE 
                '%'||#{value}||'%'
        </when>
        <when test="option == 'email' and value != null">
                "EMAIL" 
            ILIKE 
                '%'||#{value}||'%'
        </when>
        <when test="option == 'all' and value != null">
            ("USER_NAME" ILIKE '%'||#{value}||'%'
            OR "USER_ID" ILIKE '%'||#{value}||'%'
            OR "EMAIL" ILIKE '%'||#{value}||'%')
        </when>
        <when test="option == 'userAuth'">
            "USER_AUTH" = 'admin'
        </when>
    </choose>
    AND "DELETE_FLAG" = false
</where>
ORDER BY "insertDate" DESC
LIMIT
    10
OFFSET
    #{pageNo}

-- 개별 조회
SELECT
    "PK", "USER_ID", "USER_NAME", "EMAIL", "USE_FLAG", "INSERT_NAME", "UPDATE_NAME", "USER_AUTH"
    , COALESCE(to_char("LAST_ACCESS_DATE", 'YYYY-MM-DD HH24:MI:SS'), '-') AS "LAST_ACCESS_DATE" 
    , COALESCE(to_char("UPDATE_DATE", 'YYYY-MM-DD HH24:MI:SS'), '') AS "UPDATE_DATE"
    , to_char("INSERT_DATE", 'YYYY-MM-DD HH24:MI:SS') AS "INSERT_DATE"
FROM "USER" AS u 
WHERE 
    "USER_ID" = #{userId}
AND
    "DELETE_FLAG" = false

-- 삭제
DELETE
FROM
    "USER"
WHERE
<foreach collection="list" item="item" index="index" separator="OR">
    "USER_ID" = #{item}
</foreach>

-- 수정
UPDATE
    "USER"
SET
    "USER_NAME" = #{user.userName}
    , "EMAIL" = #{user.email}
    , "PW" = #{user.password}
<if test="user.userAuth != null">
        , "USER_AUTH" = #{user.userAuth}
        , "USE_FLAG" = #{user.useFlag}
</if>
, "LAST_ACCESS_DATE" = NOW()
    , "UPDATE_DATE" = NOW()
WHERE
    "USER_ID" = #{modifyingId}
AND
    "DELETE_FLAG" = false

-- 사용 여부 수정
UPDATE
    "USER"
SET
    "USE_FLAG" = NOT "USE_FLAG"
WHERE
    "USER_ID" = #{userId}

-- 아이디 찾기
SELECT 
    "USER_ID"
FROM
    "USER"
<where>
    <if test="userName != null and userName != 'null' and userName != ''">
        AND "USER_NAME" = #{userName}
    </if>
    <if test="email != null and email != 'null' and email != ''">
        AND "EMAIL" = #{email}
    </if>
    AND "DELETE_FLAG" = false
</where>


-- 임시 비밀번호 발급
UPDATE 
    "USER" 
SET
    "PW" = #{password}
WHERE 
    "USER_ID" = #{userId}
AND
    "DELETE_FLAG" = false

-- 개수 조회
SELECT 
    COUNT("PK")
FROM
    "USER"
<where>
    <choose>
        <when test="option == 'userName' and value != null">
            "USER_NAME" ILIKE '%'||#{value}||'%'
        </when>
        <when test="option == 'userId' and value != null">
            "USER_ID" ILIKE '%'||#{value}||'%'
        </when>
        <when test="option == 'email' and value != null">
            "EMAIL" ILIKE '%'||#{value}||'%'
        </when>
        <when test="option == 'all' and value != null">
            ("USER_NAME" ILIKE '%'||#{value}||'%'
            OR "USER_ID" ILIKE '%'||#{value}||'%'
            OR "EMAIL" ILIKE '%'||#{value}||'%')
        </when>
        <when test="option == 'userAuth'">
            "USER_AUTH" = 'admin'
        </when>
    </choose>
    AND "DELETE_FLAG" = false
</where>

-- 최근 접속 일자 변경
UPDATE 
    "USER"
SET 
    "LAST_ACCESS_DATE" = NOW()
WHERE 
    "USER_ID" = #{userId}

-- 로그인 하려는 사용자 정보 조회
SELECT 
    * 
FROM 
    "USER" 
WHERE 
    "USER_ID" = #{userId} 
AND 
    "PW" = #{password}
AND 
    "USE_FLAG" = true
AND
    "DELETE_FLAG" = false











--test param
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"2","condition":{"threshold":"0"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=2;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"14"}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=14;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"3","condition":{"n_bins":"5"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=3;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"4","condition":{"neg_label":"0"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=4;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"5"}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=5;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"7","condition":{"copy":"true"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=7;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"8","condition":{"feature_range":"(0,1)"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=8;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"6","condition":{"sparse_output":"true"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=6;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"9","condition":{"norm":"l1"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=9;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"10","condition":{"n_values":"auto"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=10;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"11","condition":{"categories":"auto"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=11;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"12","condition":{"with_centering":"true"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=12;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"1","condition":{"missing_values":"nan"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=1;
UPDATE "PREPROCESS_FUNCTION" SET "test_param"='{"request_test":[{"field_name":"_changeMe","preprocess_function_id":"13","condition":{"copy":"true"}}]}' where "PREPROCESS_FUNCTION_SEQUENCE_PK"=13;
