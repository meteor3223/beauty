package com.xym.beautygallery.appinfo;

/**
 * Used to cache some apps info in DB.
 */
public interface IAppsInfoCacheDatabase {
    /**
     * Get APK MD5 from cache DB.
     *
     * @return null if not found in cache DB
     */
    public String getApkMd5(String pkgName, long apkUpdateTime);

    /**
     * Save APK MD5 into cache DB
     */
    public void saveApkMd5(String pkgName, long apkUpdateTime, String apkMd5);

    /**
     * Clear APK MD5 in cache DB
     */
    public void clearApkMd5(String pkgName);

    /**
     * Get signature digest with BaiduSHA1
     *
     * @return null if not found in cache DB
     */
    public String getSignatureSha1(String pkgName, long apkUpdateTime);

    /**
     * Save signature digest with BaiduSHA1 into cache DB
     */
    public void saveSignatureSha1(String pkgName, long apkUpdateTime, String signSha1);

    /**
     * Get signature digest with BaiduMD5
     *
     * @return 0 if not found in cache DB
     */
    public long getSignatureMd5(String pkgName, long apkUpdateTime);

    /**
     * Save signature digest with BaiduMD5 into cache DB
     */
    public void saveSignatureMd5(String pkgName, long apkUpdateTime, long signMd5);

    /**
     * Get Signature file md5
     */
    public String getSignatureFileMd5(String pkgName, long apkUpdateTime);

    /**
     * Save Signature file md5
     */
    public void saveSignatureFileMd5(String pkgName, long apkUpdateTime, String sfMd5);

    /**
     * Clear Signature file md5
     */
    public void clearSignatureMd5(String pkgName);

    /**
     * Notify the cache DB when an package removed.</p>
     * Note:</br>
     * The cache DB implementation should delete DB record of the package,
     * or clear all caches (APK digests & signature digests).</p>
     *
     * @param pkgName
     */
    public void onPackageRemoved(String pkgName);
}
