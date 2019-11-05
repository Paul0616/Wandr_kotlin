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

package com.encorsa.wandr.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

/**
 * Defines methods for using the LanguageDatabaseModel class with Room.
 */
@Dao
interface WandrDatabaseDao {

    @Insert
    fun insertLanguage(languageDatabaseModel: LanguageDatabaseModel)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param languageDatabaseModel new value to write
     */
    @Update
    fun updateLanguage(languageDatabaseModel: LanguageDatabaseModel)

    @Query("SELECT * FROM languages_table WHERE languageId = :languageId LIMIT 1")
    fun findLanguageByLanguageId(languageId: String): LanguageDatabaseModel?

    @Query("SELECT * FROM languages_table WHERE rowId = :rowId LIMIT 1")
    fun findLanguageByRow(rowId: Long): LanguageDatabaseModel?

    @Query("UPDATE languages_table SET tag = :tag, name = :name WHERE rowId = :rowId")
    fun updateLanguageByRow(rowId: Long, tag: String, name: String)

    @Query("SELECT * FROM languages_table")
    fun getAllLanguages(): List<LanguageDatabaseModel>

    @Query("DELETE FROM languages_table WHERE rowId = :rowId")
    fun deleteLanguageByRow(rowId: Long)

    //LABELS -------------------------------
    @Insert
    fun insertLabel(labelDatabaseModel: LabelDatabaseModel)

    @Update
    fun updateLabel(labelDatabaseModel: LabelDatabaseModel)

    @Query("SELECT * FROM labels_table WHERE labelId = :labelId AND languageTag = :languageTag LIMIT 1")
    fun findLabelByLabelId(labelId: String, languageTag: String): LabelDatabaseModel?

    @Query("SELECT * FROM labels_table WHERE rowId = :rowId LIMIT 1")
    fun findLabelByRow(rowId: Long): LabelDatabaseModel?

    @Query("UPDATE labels_table SET tag = :tag, name = :name WHERE rowId = :rowId")
    fun updateLabelByRow(rowId: Long, tag: String, name: String)

    @Query("SELECT * FROM labels_table")
    fun getAllLabels(): List<LabelDatabaseModel>

    @Query("DELETE FROM labels_table WHERE rowId = :rowId")
    fun deleteLabelByRow(rowId: Long)

    @Query("Select * FROM labels_table WHERE tag = :tag AND languageTag = :languageTag LIMIT 1")
    fun findlabelByTag(tag: String, languageTag: String): LabelDatabaseModel?

    //OBJECTIVES ------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertObjectives(post: List<ObjectiveDatabaseModel>): List<Long>

    @RawQuery(observedEntities = [ObjectiveDatabaseModel::class])
    fun getDatabaseObjectivesWithRaw(query: SupportSQLiteQuery) : LiveData<List<ObjectiveDatabaseModel>>
}

