package com.gltech.batchcache;

import com.gltech.batchcache.TestDAOImpl.TestCompany;
import com.gltech.batchcache.TestDAOImpl.TestDateObj;
import com.gltech.batchcache.TestDAOImpl.TestObjectAfter;
import com.gltech.batchcache.TestDAOImpl.TestObjectBefore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BatchCacheTests
{
    private CacheClientImpl cacheClient;
    private TestDAO testDAO;

    //todo: test for null returns for each method.

    @Before
    public void setUp()
    {
        cacheClient = new CacheClientImpl();

        BatchCacheAspect batchCacheAspect = new BatchCacheAspect(cacheClient);
        BatchCacheEvictAspect batchCacheEvictAspect = new BatchCacheEvictAspect(cacheClient);

        TestDAO testDAOImpl = new TestDAOImpl();
        AspectJProxyFactory factory = new AspectJProxyFactory(testDAOImpl);
        factory.addAspect(batchCacheAspect);
        factory.addAspect(batchCacheEvictAspect);

        testDAO = factory.getProxy();
    }

    @After
    public void tearDown()
    {
        cacheClient.clearAll();
        cacheClient = null;
        testDAO = null;
    }

    @Test
    public void getAll()
    {
        List<TestCompany> companies = testDAO.getAllCompanies();
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getAllCompanies();
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test
    public void getByInt()
    {
        // Company 1
        TestCompany company = testDAO.getCompany(1);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Make sure cache hit/miss increment correctly
        company = testDAO.getCompany(1);
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Company 2
        TestCompany company2 = testDAO.getCompany(Integer.valueOf(2));
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(2, cacheClient.getStats().missCount());
        assertEquals(2, company2.getId());

        // Make sure cache hit/miss increment correctly
        company2 = testDAO.getCompany(Integer.valueOf(2));
        assertEquals(2, cacheClient.getStats().hitCount());
        assertEquals(2, cacheClient.getStats().missCount());
        assertEquals(2, company2.getId());
    }

    @Test
    public void getByLong()
    {
        // Company 1
        TestCompany company = testDAO.getCompany(1l);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Make sure cache hit/miss increment correctly
        company = testDAO.getCompany(1l);
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Company 2
        TestCompany company2 = testDAO.getCompany(Long.valueOf(2));
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(2, cacheClient.getStats().missCount());
        assertEquals(2, company2.getId());

        // Make sure cache hit/miss increment correctly
        company2 = testDAO.getCompany(Long.valueOf(2));
        assertEquals(2, cacheClient.getStats().hitCount());
        assertEquals(2, cacheClient.getStats().missCount());
        assertEquals(2, company2.getId());
    }

    @Test
    public void getByString()
    {
        // Company 1
        TestCompany company = testDAO.getCompany("1");
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Make sure cache hit/miss increment correctly
        company = testDAO.getCompany("1");
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());
    }

    @Test
    public void getByObject()
    {
        // Company 1
        TestCompany company = testDAO.getCompany("1");
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Make sure cache hit/miss increment correctly
        company = testDAO.getCompany("1");
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());
    }

    @Test
    public void getByDate() throws Exception
    {
        Date date = new SimpleDateFormat("MM/dd/yyyy").parse("2/1/2023");

        // DateObj 1
        TestDateObj dateObj = testDAO.getDateObj(date);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(date.getTime(), dateObj.getDate().getTime());

        // Make sure cache hit/miss increment correctly
        dateObj = testDAO.getDateObj(date);
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(date.getTime(), dateObj.getDate().getTime());
    }

    @Test
    public void getByIntLongStringMixAndMatch()
    {
        // Company 1 - int
        TestCompany company = testDAO.getCompany(1);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Company 1 - Integer
        company = testDAO.getCompany(Integer.valueOf(1));
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Company 1 - long
        company = testDAO.getCompany(1l);
        assertEquals(2, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Company 1 - Long
        company = testDAO.getCompany(Long.valueOf(1));
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());

        // Company 1 - String
        company = testDAO.getCompany("1");
        assertEquals(4, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(1, company.getId());
    }

    @Test
    public void transformObjectMismatchingId()
    {
        TestObjectBefore testObject = new TestObjectBefore(11L);

        TestCompany company = testDAO.transformObjectMismatchingId(testObject);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(11, company.getId());

        // Make sure cache hit/miss increment correctly
        company = testDAO.transformObjectMismatchingId(testObject);
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(11, company.getId());
    }

    @Test
    public void getByTestObjectString()
    {
        TestObjectBefore testObject = new TestObjectBefore(11);

        TestObjectAfter after = testDAO.transformObjectMatchingId(testObject);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(11, after.getSomeId());

        // Make sure cache hit/miss increment correctly
        after = testDAO.transformObjectMatchingId(testObject);
        assertEquals(1, cacheClient.getStats().hitCount());
        assertEquals(1, cacheClient.getStats().missCount());
        assertEquals(11, after.getSomeId());
    }

    @Test
    public void getByIntArray()
    {
        List<TestCompany> companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getCompanies(new int[]{1, 2, 3});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test
    public void getByIntegerObjArray()
    {
        Set<TestCompany> companies = testDAO.getCompanies(new Integer[]{1, 2, 3});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getCompanies(new Integer[]{1, 2, 3});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test
    public void getCompaniesIntegerList()
    {
        List<TestCompany> companies = testDAO.getCompaniesInteger(List.of(1, 2, 3));
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getCompaniesInteger(List.of(1, 2, 3));
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test
    public void getByLongArray()
    {
        Set<TestCompany> companies = testDAO.getCompanies(new long[]{1, 2, 3});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getCompanies(new long[]{1, 2, 3});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test
    public void getByLongObjArray()
    {
        List<TestCompany> companies = testDAO.getCompanies(new Long[]{1L, 2L, 3L});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getCompanies(new Long[]{1L, 2L, 3L});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test
    public void getCompaniesLongList()
    {
        List<TestCompany> companies = testDAO.getCompaniesLong(List.of(1L, 2L, 3L));
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getCompaniesLong(Set.of(1L, 2L, 3L));
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test
    public void getByStringArray()
    {
        List<TestCompany> companies = testDAO.getCompanies(new String[]{"1", "2", "3"});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getCompanies(new String[]{"1", "2", "3"});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test
    public void getCompaniesStringList()
    {
        Set<TestCompany> companies = testDAO.getCompaniesString(List.of("1", "2", "3"));
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());

        companies = testDAO.getCompaniesString(List.of("1", "2", "3"));
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.size());
    }

    @Test(expected = IllegalStateException.class)
    public void transformObjectMismatchingIds()
    {
        List<TestObjectBefore> testBeforeObjects = List.of(new TestObjectBefore(1L), new TestObjectBefore(2L), new TestObjectBefore(3L));
        List<TestCompany> companies = testDAO.transformObjectMismatchingIds(testBeforeObjects);
    }

    @Test
    public void transformObjectMatchingIds()
    {
        List<TestObjectBefore> testBeforeObjects = List.of(new TestObjectBefore(1L), new TestObjectBefore(2L), new TestObjectBefore(3L));

        List<TestObjectAfter> testAfterObjects = testDAO.transformObjectMatchingIds(testBeforeObjects);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, testAfterObjects.size());

        testAfterObjects = testDAO.transformObjectMatchingIds(testBeforeObjects);
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, testAfterObjects.size());
    }

    @Test
    public void getCompaniesMapIntArray()
    {
        Map<Integer, TestCompany> companies = testDAO.getCompaniesMap(new int[]{1, 2, 3});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2).getId());

        companies = testDAO.getCompaniesMap(new int[]{1, 2, 3});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2).getId());
    }

    @Test
    public void getCompaniesMapListIntArray()
    {
        Map<Integer, List<TestCompany>> companies = testDAO.getCompaniesMapList(new int[]{1, 2, 3});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2).get(0).getId());

        companies = testDAO.getCompaniesMapList(new int[]{1, 2, 3});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2).get(0).getId());
    }

    @Test
    public void getCompaniesMapIntegerObjArray()
    {
        Map<Integer, TestCompany> companies = testDAO.getCompaniesMap(new Integer[]{1, 2, 3});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2).getId());

        companies = testDAO.getCompaniesMap(new Integer[]{1, 2, 3});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2).getId());
    }

    @Test
    public void getCompaniesMapIntegerObjList()
    {
        Map<Integer, TestCompany> companies = testDAO.getCompaniesMapInteger(List.of(1, 2, 3));
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2).getId());

        companies = testDAO.getCompaniesMapInteger(List.of(1, 2, 3));
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2).getId());
    }

    @Test
    public void getCompaniesMapLongArray()
    {
        Map<Long, TestCompany> companies = testDAO.getCompaniesMap(new long[]{1L, 2L, 3L});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2L).getId());

        companies = testDAO.getCompaniesMap(new long[]{1L, 2L, 3L});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2L).getId());
    }

    @Test
    public void getCompaniesMapLongObjArray()
    {
        Map<Long, TestCompany> companies = testDAO.getCompaniesMap(new Long[]{1L, 2L, 3L});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2L).getId());

        companies = testDAO.getCompaniesMap(new Long[]{1L, 2L, 3L});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2L).getId());
    }

    @Test
    public void getCompaniesMapLongObjList()
    {
        Map<Long, TestCompany> companies = testDAO.getCompaniesMapLong(List.of(1L, 2L, 3L));
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2L).getId());

        companies = testDAO.getCompaniesMapLong(List.of(1L, 2L, 3L));
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get(2L).getId());
    }

    @Test
    public void getCompaniesMapStringObjArray()
    {
        Map<String, TestCompany> companies = testDAO.getCompaniesMap(new String[]{"1", "2", "3"});
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get("2").getId());

        companies = testDAO.getCompaniesMap(new String[]{"1", "2", "3"});
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get("2").getId());
    }

    @Test
    public void getCompaniesMapStringObjList()
    {
        Map<String, TestCompany> companies = testDAO.getCompaniesMapString(List.of("1", "2", "3"));
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get("2").getId());

        companies = testDAO.getCompaniesMapString(List.of("1", "2", "3"));
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, companies.keySet().size());
        assertEquals(2, companies.get("2").getId());
    }


    @Test
    public void transformObjectMismatchingIdsMap()
    {
        List<TestObjectBefore> testBeforeObjects = List.of(new TestObjectBefore(1L), new TestObjectBefore(2L), new TestObjectBefore(3L));

        Map<TestObjectBefore, TestCompany> testAfterObjects = testDAO.transformObjectMismatchingIdsMap(testBeforeObjects);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, testAfterObjects.size());
        assertEquals(2, testAfterObjects.get(new TestObjectBefore(2L)).getId());

        testAfterObjects = testDAO.transformObjectMismatchingIdsMap(testBeforeObjects);
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, testAfterObjects.size());
        assertEquals(2, testAfterObjects.get(new TestObjectBefore(2L)).getId());
    }

    @Test
    public void transformObjectMatchingIdsMap()
    {
        List<TestObjectBefore> testBeforeObjects = List.of(new TestObjectBefore(1L), new TestObjectBefore(2L), new TestObjectBefore(3L));

        Map<TestObjectBefore, TestObjectAfter> testAfterObjects = testDAO.transformObjectMatchingIdsMap(testBeforeObjects);
        assertEquals(0, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, testAfterObjects.size());
        assertEquals(2, testAfterObjects.get(new TestObjectBefore(2L)).getSomeId());

        testAfterObjects = testDAO.transformObjectMatchingIdsMap(testBeforeObjects);
        assertEquals(3, cacheClient.getStats().hitCount());
        assertEquals(3, cacheClient.getStats().missCount());
        assertEquals(3, testAfterObjects.size());
        assertEquals(2, testAfterObjects.get(new TestObjectBefore(2L)).getSomeId());
    }
}