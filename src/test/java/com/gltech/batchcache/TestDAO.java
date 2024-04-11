package com.gltech.batchcache;

import java.util.*;

public interface TestDAO
{
    List<TestDAOImpl.TestCompany> getAllCompanies();

    void clearAllCompanies();

    TestDAOImpl.TestCompany getCompany(int id);

    TestDAOImpl.TestCompany getCompany(Integer id);

    TestDAOImpl.TestCompany getCompany(long id);

    TestDAOImpl.TestCompany getCompany(Long id);

    TestDAOImpl.TestCompany getCompany(String id);

    TestDAOImpl.TestCompany getNullParameter(String test);

    TestDAOImpl.TestObjectAfter transformObjectMatchingId(TestDAOImpl.TestObjectBefore testObject);

    TestDAOImpl.TestCompany transformObjectMismatchingId(TestDAOImpl.TestObjectBefore testObject);

    TestDAOImpl.TestDateObj getDateObj(Date date);

    List<TestDAOImpl.TestCompany> getCompanies(int[] ids);

    Set<TestDAOImpl.TestCompany> getCompanies(Integer[] ids);

    List<TestDAOImpl.TestCompany> getCompaniesInteger(Collection<Integer> ids);

    Set<TestDAOImpl.TestCompany> getCompanies(long[] ids);

    List<TestDAOImpl.TestCompany> getCompanies(Long[] ids);

    List<TestDAOImpl.TestCompany> getCompaniesLong(Collection<Long> ids);

    List<TestDAOImpl.TestCompany> getCompanies(String[] ids);

    Set<TestDAOImpl.TestCompany> getCompaniesString(Collection<String> ids);

    List<TestDAOImpl.TestObjectAfter> transformObjectMatchingIds(Collection<TestDAOImpl.TestObjectBefore> testObjects);

    List<TestDAOImpl.TestCompany> transformObjectMismatchingIds(Collection<TestDAOImpl.TestObjectBefore> testObjects);

    Map<Integer, TestDAOImpl.TestCompany> getCompaniesMap(int[] ids);

    Map<Integer, List<TestDAOImpl.TestCompany>> getCompaniesMapList(int[] ids);

    Map<Integer, TestDAOImpl.TestCompany> getCompaniesMap(Integer[] ids);

    Map<Integer, TestDAOImpl.TestCompany> getCompaniesMapInteger(Collection<Integer> ids);

    Map<Long, TestDAOImpl.TestCompany> getCompaniesMap(long[] ids);

    Map<Long, TestDAOImpl.TestCompany> getCompaniesMap(Long[] ids);

    Map<Long, TestDAOImpl.TestCompany> getCompaniesMapLong(Collection<Long> ids);

    Map<String, TestDAOImpl.TestCompany> getCompaniesMap(String[] ids);

    Map<String, TestDAOImpl.TestCompany> getCompaniesMapString(Collection<String> ids);

    Map<TestDAOImpl.TestObjectBefore, TestDAOImpl.TestCompany> transformObjectMismatchingIdsMap(Collection<TestDAOImpl.TestObjectBefore> testObjects);

    Map<TestDAOImpl.TestObjectBefore, TestDAOImpl.TestObjectAfter> transformObjectMatchingIdsMap(Collection<TestDAOImpl.TestObjectBefore> testObjects);

    void save(TestDAOImpl.TestCompany company);

    void save(TestDAOImpl.TestDateObj dateObj);

    void save(Collection<TestDAOImpl.TestCompany> company);

    void delete(int id);

    void delete(int id, int someIgnoredId);

    void refresh(Integer id);

    void delete(int[] ids);

    void refresh(Integer[] ids);

    void clearIntegers(Collection<Integer> ids);

    void delete(long id);

    void refresh(Long id);

    void delete(long[] ids);

    void refresh(Long[] ids);

    void clearLongs(Collection<Long> ids);

    void delete(String id);

    void refresh(String[] ids);

    void clearStrings(Collection<String> ids);

    void clearTestCompanies(Collection<TestDAOImpl.TestCompany> testCompanies);

    void clearTransformation(TestDAOImpl.TestObjectBefore testObject);

    void clearTransformations(Collection<TestDAOImpl.TestObjectBefore> testObjects);

    void evictMultipleKeys(int id);

    void evictMultipleKeys(TestDAOImpl.TestObjectBefore testObject);

    void evictMultipleKeys(Collection<TestDAOImpl.TestObjectBefore> testObjects);

    void handleNull(String nullParameter);
}