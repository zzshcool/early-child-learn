# 使用多階段構建來優化映像大小
# 階段 1: 建置階段
FROM maven:3.9-eclipse-temurin-17 AS build

# 設定工作目錄
WORKDIR /app

# 複製 pom.xml 並下載依賴（利用 Docker 快取）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 複製源代碼
COPY src ./src

# 建置應用程式（跳過測試以加快建置速度）
RUN mvn clean package -DskipTests

# 階段 2: 運行階段
# 使用 IBM Semeru Runtimes (OpenJ9) 以獲得極致的記憶體效率
FROM ibm-semeru-runtimes:open-17-jre

# 設定工作目錄
WORKDIR /app

# 複製建置好的 JAR 檔案
COPY --from=build /app/target/*.jar app.jar

# 暴露應用程式端口（Spring Boot 預設為 8080）
EXPOSE 8080

# 設定 JVM 參數以優化容器環境 (OpenJ9 極致優化)
# -Xquickstart: 犧牲峰值吞吐量換取快速啟動和低編譯活動
# -Xtune:virtualized: 針對容器/虛擬化環境優化
# -Xmx64m: 限制最大堆積記憶體 (OpenJ9 對 Heap 的利用率更高，可以設得更小)
# -Xss256k: 減少執行緒堆疊大小
ENV JAVA_OPTS="-Xmx64m -Xss256k -Xquickstart -Xtune:virtualized -noverify"

# 執行應用程式
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
