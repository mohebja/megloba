package com.global.sms

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.enterprise.EnterpriseOrganizationManager
import com.global.sms.data.dao.DepartmentDao
import com.global.sms.data.dao.EmployeeDao
import com.global.sms.data.dao.OrganizationDao
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.DepartmentEntity
import com.global.sms.data.entity.EmployeeEntity
import com.global.sms.data.entity.OrganizationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EnterpriseOrganizationRealPersistenceTest {

    private lateinit var db: GlobalSmsDatabase
    private lateinit var orgDao: OrganizationDao
    private lateinit var departmentDao: DepartmentDao
    private lateinit var employeeDao: EmployeeDao
    private lateinit var orgManager: EnterpriseOrganizationManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GlobalSmsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        orgDao = db.organizationDao()
        departmentDao = db.departmentDao()
        employeeDao = db.employeeDao()
        orgManager = EnterpriseOrganizationManager(orgDao, departmentDao, employeeDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testEmptyDatabaseStartsWithoutHardcodedData() = runBlocking {
        val initialDepts = orgManager.departments.first()
        val initialEmps = orgManager.employees.first()

        assertTrue("Departments should be initially empty on a fresh database", initialDepts.isEmpty())
        assertTrue("Employees should be initially empty on a fresh database", initialEmps.isEmpty())
    }

    @Test
    fun testOrganizationCreationAndPersistence() = runBlocking {
        val created = orgManager.createOrganization("شرکت توسعه ارتباطات سپهر", "فناوری اطلاعات و ارتباطات")
        assertEquals("شرکت توسعه ارتباطات سپهر", created.companyName)

        val persisted = orgDao.getOrganization()
        assertNotNull(persisted)
        assertEquals("شرکت توسعه ارتباطات سپهر", persisted?.companyName)
        assertEquals("فناوری اطلاعات و ارتباطات", persisted?.organizationType)
    }

    @Test
    fun testDepartmentCrudPersistence() = runBlocking {
        val dep1 = orgManager.addDepartment("دپارتمان مهندسی نرم‌افزار", "مهندس اکبری")
        val dep2 = orgManager.addDepartment("دپارتمان امنیت داده", "دکتر کاویانی")

        val depts = orgManager.departments.first()
        assertEquals(2, depts.size)
        assertTrue(depts.any { it.name == "دپارتمان مهندسی نرم‌افزار" })
        assertTrue(depts.any { it.name == "دپارتمان امنیت داده" })

        // Delete department
        orgManager.deleteDepartment(dep1.id)
        val deptsAfterDelete = orgManager.departments.first()
        assertEquals(1, deptsAfterDelete.size)
        assertEquals("دپارتمان امنیت داده", deptsAfterDelete.first().name)
    }

    @Test
    fun testEmployeeCrudAndPermissionsPersistence() = runBlocking {
        val dep = orgManager.addDepartment("پشتیبانی فنی", "علی رضایی")

        val emp = orgManager.addEmployee(
            departmentId = dep.id,
            name = "سارا احمدی",
            role = "SUPPORT_SPECIALIST",
            permissions = listOf("SEND_SMS", "VIEW_LOGS")
        )

        val emps = orgManager.employees.first()
        assertEquals(1, emps.size)
        assertEquals("سارا احمدی", emps.first().name)
        assertEquals(listOf("SEND_SMS", "VIEW_LOGS"), emps.first().permissions)

        // Update permissions
        val updateSuccess = orgManager.updateEmployeePermissions(emp.id, listOf("SEND_SMS", "VIEW_LOGS", "MANAGE_TEMPLATES"))
        assertTrue(updateSuccess)

        val updatedEmps = orgManager.employees.first()
        assertEquals(listOf("SEND_SMS", "VIEW_LOGS", "MANAGE_TEMPLATES"), updatedEmps.first().permissions)

        // Delete employee
        orgManager.deleteEmployee(emp.id)
        val empsAfterDelete = orgManager.employees.first()
        assertTrue(empsAfterDelete.isEmpty())
    }
}
