/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.encorsa.wandr

import android.util.Log
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.encorsa.wandr.database.*
import com.encorsa.wandr.network.models.LabelModel
import com.encorsa.wandr.network.models.LanguageAndNameModel

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import androidx.sqlite.db.SupportSQLiteQueryBuilder


/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class)
class WandrDatabaseTest {

    private lateinit var wandrDao: WandrDatabaseDao
    private lateinit var db: WandrDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, WandrDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        wandrDao = db.wandrDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

//    @Test
//    @Throws(Exception::class)
//    fun insertAndGetObjective() {
//        val obj1 = ObjectiveDatabaseModel(
//            "aa",
//            "subcat1",
//            "cat1",
//            "RO",
//            "name1",
//            "addres1",
//            "longDes1",
//            false,
//            1.1,
//            2.2,
//            "url1",
//            "email1",
//            "phone1"
//        )
//        val obj2 = ObjectiveDatabaseModel(
//            "bb",
//            "subcat2",
//            "cat2",
//            "RO",
//            "name2",
//            "addres2",
//            "longDes2",
//            true,
//            3.1,
//            4.2,
//            "url2",
//            "email2",
//            "phone2"
//        )
//        var listObj = ArrayList<ObjectiveDatabaseModel>()
//        listObj.add(obj1)
//        listObj.add(obj2)
//        wandrDao.insertObjectives(listObj)
//
//        val languageTag = "RO"
//        val onlyFavs = null
//        val name = null
//        val categoryId = null
//        val subcategoryIds = null//arrayOf<Any>("subcat1","subcat2")
//        val query = getQuery(languageTag, onlyFavs, name, categoryId, subcategoryIds)
//        val x = query.sql
//
//        val obj = wandrDao.getDatabaseObjectivesWithRaw(query)
//        Log.i("WandrDatabaseTest", obj.value.toList().toString())
//}
//
//    private fun getQuery(
//        languageTag: String,
//        onlyFavs: Boolean?,
//        name: String?,
//        categoryId: String?,
//        subcategoryIds: Array<Any>?
//    ): SupportSQLiteQuery {
//        val bindArgs = ArrayList<Any>()
//
//        var stringSelection = ""
//        val builder = SupportSQLiteQueryBuilder
//            .builder("objectives_table")
//            .columns(arrayOf("*"))
//
//        if (!languageTag.isEmpty()) {
//            bindArgs.add(languageTag)
//            stringSelection += " languageTag = ?"
//        }
//
//        if (onlyFavs != null) {
//            bindArgs.add(onlyFavs)
//            stringSelection += " AND isFavorite = ?"
//        }
//
//        if (name != null) {
//            bindArgs.add("%".plus(name).plus("%"))
//            stringSelection += " AND name LIKE ?"
//        }
//
//        if (categoryId != null) {
//            bindArgs.add(categoryId!!)
//            stringSelection += " AND categoryId = ?"
//        }
//
//        if (subcategoryIds != null) {
//            val str = "?,".repeat(subcategoryIds.size).dropLast(1)
//            bindArgs.addAll(subcategoryIds)
//            stringSelection += " AND subcategoryId IN (${str})"
//        }
//
//        if (!stringSelection.equals(""))
//            builder.selection(stringSelection, bindArgs.toArray())
//        val query = builder.create()
//
//        Log.i("WandrDatabaseTest", query.sql + " " + bindArgs.toString())
//
//        return query
//    }

    //@Test
@Throws(Exception::class)
fun insertAndGetNight() {
    val language1 = LanguageDatabaseModel(1, "RO", "Romana", "123aBC")
    wandrDao.insertLanguage(language1)
    val language2 = wandrDao.findLanguageByLanguageId("123aBC")
    language2?.name = "Turca"
    wandrDao.updateLanguage(language2!!)
    val currentLanguageRow = wandrDao.findLanguageByLanguageId("123aBC")
    val currentLanguage = wandrDao.findLanguageByRow(currentLanguageRow?.rowId!!)
    Assert.assertEquals(currentLanguage?.tag, "RO")
    Assert.assertEquals(currentLanguage?.name, "Turca")
    val group: List<LanguageDatabaseModel> = wandrDao.getAllLanguages()
    wandrDao.updateLanguageByRow(currentLanguageRow?.rowId!!, "BG", "Bulgara")
    val group1 = wandrDao.getAllLanguages()

//        val label1 = LabelDatabaseModel(1, "testTagLabel", "lanelName", "123aBC", "labelId")
//        wandrDao.insertLabel(label1)
//        val label2 = wandrDao.findLabelByLabelId("labelId", )
//        label2?.name = "labelName"
//        wandrDao.updateLabel(label2!!)
//        val currentLabelRow = wandrDao.findLabelByLabelId("labelId")
//        val currentLabel = wandrDao.findLabelByRow(currentLabelRow?.rowId!!)
//        Assert.assertEquals(currentLabel?.tag, "testTagLabel")
//        Assert.assertEquals(currentLabel?.name, "labelName")
//        val groupL: List<LabelDatabaseModel> = wandrDao.getAllLabels()
//        wandrDao.updateLabelByRow(currentLabelRow?.rowId!!, "2", "2A")
//        val group1L = wandrDao.getAllLabels()
}

//@Test
@Throws(Exception::class)
fun synchronizeLanguages() {
    wandrDao.insertLanguage(LanguageDatabaseModel(0, "DEL", "FOR DELETE", "idDEL"))
    val apiLanguages = listOf(
        LanguageDatabaseModel(0, "RO", "Romana", "idRO"),
        LanguageDatabaseModel(0, "BG", "Bulgara", "idBG"),
        LanguageDatabaseModel(0, "EN", "Engleza", "idEN")
    )
    for (languageDatabaseModel: LanguageDatabaseModel in apiLanguages) {
        val foundedDatabaseLang =
            wandrDao.findLanguageByLanguageId(languageDatabaseModel.languageId)
        if (null == foundedDatabaseLang) {
            wandrDao.insertLanguage(languageDatabaseModel)
        } else {
            wandrDao.updateLanguageByRow(
                foundedDatabaseLang.rowId,
                foundedDatabaseLang.tag,
                foundedDatabaseLang.name
            )
        }
    }
    var allDatabaseLanguages = wandrDao.getAllLanguages()
    for (langDb in allDatabaseLanguages) {
        var wasFoundInApiLanguage = false
        for (langApi in apiLanguages) {
            if (langApi.languageId == langDb.languageId) {
                wasFoundInApiLanguage = true
                break
            }
        }
        if (!wasFoundInApiLanguage) {
            wandrDao.deleteLanguageByRow(langDb.rowId)
        }
    }
    allDatabaseLanguages = wandrDao.getAllLanguages()
}

//@Test
@Throws(Exception::class)
fun synchronizeLabels() {
    wandrDao.insertLabel(LabelDatabaseModel(0, "gchach", "jhx bjhx", "x b", "agsvxhg"))
    val apiLabels = listOf(
        LabelModel(
            "id1", "tag1", listOf(
                LanguageAndNameModel("RO", "val1RO"),
                LanguageAndNameModel("BG", "val1BG"),
                LanguageAndNameModel("EN", "val1EN")
            )
        ),
        LabelModel(
            "id2", "tag2", listOf(
                LanguageAndNameModel("RO", "val2RO"),
                LanguageAndNameModel("BG", "val2BG"),
                LanguageAndNameModel("EN", "val2EN")
            )
        ),
        LabelModel(
            "id3", "tag3", listOf(
                LanguageAndNameModel("RO", "val3RO"),
                LanguageAndNameModel("BG", "val3BG"),
                LanguageAndNameModel("EN", "val3EN")
            )
        )
    )

    for (labelModel in apiLabels) {
        for (languageAndNameModel in labelModel.labelNames) {
            val label =
                wandrDao.findLabelByLabelId(labelModel.id, languageAndNameModel.language)
            if (null == label) {
                val newLabel = LabelDatabaseModel(
                    0,
                    labelModel.tag,
                    languageAndNameModel.name,
                    languageAndNameModel.language,
                    labelModel.id
                )
                wandrDao.insertLabel(newLabel)
            } else {
                wandrDao.updateLabelByRow(
                    label.rowId,
                    labelModel.tag,
                    languageAndNameModel.name!!
                )
            }
        }
    }
    var allLabels = wandrDao.getAllLabels()
    for (label in allLabels) {
        val labelId = label.labelId
        var wasFound = false
        for (labelModel in apiLabels) {
            for (languageAndNameModel in labelModel.labelNames) {
                if (label.labelId == labelModel.id && label.languageTag == languageAndNameModel.language) {
                    wasFound = true
                    break
                }
            }
            if (wasFound) {
                break
            }
        }
        if (!wasFound) {
            wandrDao.deleteLabelByRow(label.rowId)
        }
    }
    allLabels = wandrDao.getAllLabels()
}


}

