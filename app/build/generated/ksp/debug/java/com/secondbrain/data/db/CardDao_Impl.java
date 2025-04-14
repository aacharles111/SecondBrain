package com.secondbrain.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.secondbrain.data.model.Card;
import com.secondbrain.data.model.CardSearchResult;
import com.secondbrain.data.model.CardType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CardDao_Impl implements CardDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Card> __insertionAdapterOfCard;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Card> __updateAdapterOfCard;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCardById;

  public CardDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCard = new EntityInsertionAdapter<Card>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cards` (`id`,`title`,`content`,`summary`,`type`,`source`,`tags`,`createdAt`,`updatedAt`,`language`,`aiModel`,`summaryType`,`thumbnailUrl`,`pageCount`,`videoId`,`channelTitle`,`videoDuration`,`viewCount`,`hasTranscript`,`metadata`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Card entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getContent());
        statement.bindString(4, entity.getSummary());
        final String _tmp = __converters.fromCardType(entity.getType());
        statement.bindString(5, _tmp);
        statement.bindString(6, entity.getSource());
        final String _tmp_1 = __converters.fromStringList(entity.getTags());
        statement.bindString(7, _tmp_1);
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindString(10, entity.getLanguage());
        statement.bindString(11, entity.getAiModel());
        statement.bindString(12, entity.getSummaryType());
        if (entity.getThumbnailUrl() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getThumbnailUrl());
        }
        if (entity.getPageCount() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getPageCount());
        }
        if (entity.getVideoId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getVideoId());
        }
        if (entity.getChannelTitle() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getChannelTitle());
        }
        if (entity.getVideoDuration() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getVideoDuration());
        }
        if (entity.getViewCount() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getViewCount());
        }
        final int _tmp_2 = entity.getHasTranscript() ? 1 : 0;
        statement.bindLong(19, _tmp_2);
        if (entity.getMetadata() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getMetadata());
        }
      }
    };
    this.__updateAdapterOfCard = new EntityDeletionOrUpdateAdapter<Card>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `cards` SET `id` = ?,`title` = ?,`content` = ?,`summary` = ?,`type` = ?,`source` = ?,`tags` = ?,`createdAt` = ?,`updatedAt` = ?,`language` = ?,`aiModel` = ?,`summaryType` = ?,`thumbnailUrl` = ?,`pageCount` = ?,`videoId` = ?,`channelTitle` = ?,`videoDuration` = ?,`viewCount` = ?,`hasTranscript` = ?,`metadata` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Card entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getContent());
        statement.bindString(4, entity.getSummary());
        final String _tmp = __converters.fromCardType(entity.getType());
        statement.bindString(5, _tmp);
        statement.bindString(6, entity.getSource());
        final String _tmp_1 = __converters.fromStringList(entity.getTags());
        statement.bindString(7, _tmp_1);
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindString(10, entity.getLanguage());
        statement.bindString(11, entity.getAiModel());
        statement.bindString(12, entity.getSummaryType());
        if (entity.getThumbnailUrl() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getThumbnailUrl());
        }
        if (entity.getPageCount() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getPageCount());
        }
        if (entity.getVideoId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getVideoId());
        }
        if (entity.getChannelTitle() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getChannelTitle());
        }
        if (entity.getVideoDuration() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getVideoDuration());
        }
        if (entity.getViewCount() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getViewCount());
        }
        final int _tmp_2 = entity.getHasTranscript() ? 1 : 0;
        statement.bindLong(19, _tmp_2);
        if (entity.getMetadata() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getMetadata());
        }
        statement.bindString(21, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCardById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cards WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCard(final Card card, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCard.insert(card);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCard(final Card card, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCard.handle(card);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCardById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCardById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCardById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Card>> getAllCards() {
    final String _sql = "SELECT * FROM cards ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cards"}, new Callable<List<Card>>() {
      @Override
      @NonNull
      public List<Card> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfAiModel = CursorUtil.getColumnIndexOrThrow(_cursor, "aiModel");
          final int _cursorIndexOfSummaryType = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryType");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "pageCount");
          final int _cursorIndexOfVideoId = CursorUtil.getColumnIndexOrThrow(_cursor, "videoId");
          final int _cursorIndexOfChannelTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "channelTitle");
          final int _cursorIndexOfVideoDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "videoDuration");
          final int _cursorIndexOfViewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "viewCount");
          final int _cursorIndexOfHasTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "hasTranscript");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<Card> _result = new ArrayList<Card>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Card _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final CardType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toCardType(_tmp);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final List<String> _tmpTags;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp_1);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final String _tmpAiModel;
            _tmpAiModel = _cursor.getString(_cursorIndexOfAiModel);
            final String _tmpSummaryType;
            _tmpSummaryType = _cursor.getString(_cursorIndexOfSummaryType);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final Integer _tmpPageCount;
            if (_cursor.isNull(_cursorIndexOfPageCount)) {
              _tmpPageCount = null;
            } else {
              _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            }
            final String _tmpVideoId;
            if (_cursor.isNull(_cursorIndexOfVideoId)) {
              _tmpVideoId = null;
            } else {
              _tmpVideoId = _cursor.getString(_cursorIndexOfVideoId);
            }
            final String _tmpChannelTitle;
            if (_cursor.isNull(_cursorIndexOfChannelTitle)) {
              _tmpChannelTitle = null;
            } else {
              _tmpChannelTitle = _cursor.getString(_cursorIndexOfChannelTitle);
            }
            final String _tmpVideoDuration;
            if (_cursor.isNull(_cursorIndexOfVideoDuration)) {
              _tmpVideoDuration = null;
            } else {
              _tmpVideoDuration = _cursor.getString(_cursorIndexOfVideoDuration);
            }
            final String _tmpViewCount;
            if (_cursor.isNull(_cursorIndexOfViewCount)) {
              _tmpViewCount = null;
            } else {
              _tmpViewCount = _cursor.getString(_cursorIndexOfViewCount);
            }
            final boolean _tmpHasTranscript;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfHasTranscript);
            _tmpHasTranscript = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new Card(_tmpId,_tmpTitle,_tmpContent,_tmpSummary,_tmpType,_tmpSource,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt,_tmpLanguage,_tmpAiModel,_tmpSummaryType,_tmpThumbnailUrl,_tmpPageCount,_tmpVideoId,_tmpChannelTitle,_tmpVideoDuration,_tmpViewCount,_tmpHasTranscript,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Card> getCardById(final String id) {
    final String _sql = "SELECT * FROM cards WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cards"}, new Callable<Card>() {
      @Override
      @Nullable
      public Card call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfAiModel = CursorUtil.getColumnIndexOrThrow(_cursor, "aiModel");
          final int _cursorIndexOfSummaryType = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryType");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "pageCount");
          final int _cursorIndexOfVideoId = CursorUtil.getColumnIndexOrThrow(_cursor, "videoId");
          final int _cursorIndexOfChannelTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "channelTitle");
          final int _cursorIndexOfVideoDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "videoDuration");
          final int _cursorIndexOfViewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "viewCount");
          final int _cursorIndexOfHasTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "hasTranscript");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final Card _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final CardType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toCardType(_tmp);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final List<String> _tmpTags;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp_1);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final String _tmpAiModel;
            _tmpAiModel = _cursor.getString(_cursorIndexOfAiModel);
            final String _tmpSummaryType;
            _tmpSummaryType = _cursor.getString(_cursorIndexOfSummaryType);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final Integer _tmpPageCount;
            if (_cursor.isNull(_cursorIndexOfPageCount)) {
              _tmpPageCount = null;
            } else {
              _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            }
            final String _tmpVideoId;
            if (_cursor.isNull(_cursorIndexOfVideoId)) {
              _tmpVideoId = null;
            } else {
              _tmpVideoId = _cursor.getString(_cursorIndexOfVideoId);
            }
            final String _tmpChannelTitle;
            if (_cursor.isNull(_cursorIndexOfChannelTitle)) {
              _tmpChannelTitle = null;
            } else {
              _tmpChannelTitle = _cursor.getString(_cursorIndexOfChannelTitle);
            }
            final String _tmpVideoDuration;
            if (_cursor.isNull(_cursorIndexOfVideoDuration)) {
              _tmpVideoDuration = null;
            } else {
              _tmpVideoDuration = _cursor.getString(_cursorIndexOfVideoDuration);
            }
            final String _tmpViewCount;
            if (_cursor.isNull(_cursorIndexOfViewCount)) {
              _tmpViewCount = null;
            } else {
              _tmpViewCount = _cursor.getString(_cursorIndexOfViewCount);
            }
            final boolean _tmpHasTranscript;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfHasTranscript);
            _tmpHasTranscript = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _result = new Card(_tmpId,_tmpTitle,_tmpContent,_tmpSummary,_tmpType,_tmpSource,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt,_tmpLanguage,_tmpAiModel,_tmpSummaryType,_tmpThumbnailUrl,_tmpPageCount,_tmpVideoId,_tmpChannelTitle,_tmpVideoDuration,_tmpViewCount,_tmpHasTranscript,_tmpMetadata);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCardByIdSync(final String id, final Continuation<? super Card> $completion) {
    final String _sql = "SELECT * FROM cards WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Card>() {
      @Override
      @Nullable
      public Card call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfAiModel = CursorUtil.getColumnIndexOrThrow(_cursor, "aiModel");
          final int _cursorIndexOfSummaryType = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryType");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "pageCount");
          final int _cursorIndexOfVideoId = CursorUtil.getColumnIndexOrThrow(_cursor, "videoId");
          final int _cursorIndexOfChannelTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "channelTitle");
          final int _cursorIndexOfVideoDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "videoDuration");
          final int _cursorIndexOfViewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "viewCount");
          final int _cursorIndexOfHasTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "hasTranscript");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final Card _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final CardType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toCardType(_tmp);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final List<String> _tmpTags;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp_1);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final String _tmpAiModel;
            _tmpAiModel = _cursor.getString(_cursorIndexOfAiModel);
            final String _tmpSummaryType;
            _tmpSummaryType = _cursor.getString(_cursorIndexOfSummaryType);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final Integer _tmpPageCount;
            if (_cursor.isNull(_cursorIndexOfPageCount)) {
              _tmpPageCount = null;
            } else {
              _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            }
            final String _tmpVideoId;
            if (_cursor.isNull(_cursorIndexOfVideoId)) {
              _tmpVideoId = null;
            } else {
              _tmpVideoId = _cursor.getString(_cursorIndexOfVideoId);
            }
            final String _tmpChannelTitle;
            if (_cursor.isNull(_cursorIndexOfChannelTitle)) {
              _tmpChannelTitle = null;
            } else {
              _tmpChannelTitle = _cursor.getString(_cursorIndexOfChannelTitle);
            }
            final String _tmpVideoDuration;
            if (_cursor.isNull(_cursorIndexOfVideoDuration)) {
              _tmpVideoDuration = null;
            } else {
              _tmpVideoDuration = _cursor.getString(_cursorIndexOfVideoDuration);
            }
            final String _tmpViewCount;
            if (_cursor.isNull(_cursorIndexOfViewCount)) {
              _tmpViewCount = null;
            } else {
              _tmpViewCount = _cursor.getString(_cursorIndexOfViewCount);
            }
            final boolean _tmpHasTranscript;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfHasTranscript);
            _tmpHasTranscript = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _result = new Card(_tmpId,_tmpTitle,_tmpContent,_tmpSummary,_tmpType,_tmpSource,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt,_tmpLanguage,_tmpAiModel,_tmpSummaryType,_tmpThumbnailUrl,_tmpPageCount,_tmpVideoId,_tmpChannelTitle,_tmpVideoDuration,_tmpViewCount,_tmpHasTranscript,_tmpMetadata);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Card>> getCardsByTag(final String tag) {
    final String _sql = "SELECT * FROM cards WHERE tags LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, tag);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cards"}, new Callable<List<Card>>() {
      @Override
      @NonNull
      public List<Card> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfAiModel = CursorUtil.getColumnIndexOrThrow(_cursor, "aiModel");
          final int _cursorIndexOfSummaryType = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryType");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "pageCount");
          final int _cursorIndexOfVideoId = CursorUtil.getColumnIndexOrThrow(_cursor, "videoId");
          final int _cursorIndexOfChannelTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "channelTitle");
          final int _cursorIndexOfVideoDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "videoDuration");
          final int _cursorIndexOfViewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "viewCount");
          final int _cursorIndexOfHasTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "hasTranscript");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<Card> _result = new ArrayList<Card>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Card _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final CardType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toCardType(_tmp);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final List<String> _tmpTags;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp_1);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final String _tmpAiModel;
            _tmpAiModel = _cursor.getString(_cursorIndexOfAiModel);
            final String _tmpSummaryType;
            _tmpSummaryType = _cursor.getString(_cursorIndexOfSummaryType);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final Integer _tmpPageCount;
            if (_cursor.isNull(_cursorIndexOfPageCount)) {
              _tmpPageCount = null;
            } else {
              _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            }
            final String _tmpVideoId;
            if (_cursor.isNull(_cursorIndexOfVideoId)) {
              _tmpVideoId = null;
            } else {
              _tmpVideoId = _cursor.getString(_cursorIndexOfVideoId);
            }
            final String _tmpChannelTitle;
            if (_cursor.isNull(_cursorIndexOfChannelTitle)) {
              _tmpChannelTitle = null;
            } else {
              _tmpChannelTitle = _cursor.getString(_cursorIndexOfChannelTitle);
            }
            final String _tmpVideoDuration;
            if (_cursor.isNull(_cursorIndexOfVideoDuration)) {
              _tmpVideoDuration = null;
            } else {
              _tmpVideoDuration = _cursor.getString(_cursorIndexOfVideoDuration);
            }
            final String _tmpViewCount;
            if (_cursor.isNull(_cursorIndexOfViewCount)) {
              _tmpViewCount = null;
            } else {
              _tmpViewCount = _cursor.getString(_cursorIndexOfViewCount);
            }
            final boolean _tmpHasTranscript;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfHasTranscript);
            _tmpHasTranscript = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new Card(_tmpId,_tmpTitle,_tmpContent,_tmpSummary,_tmpType,_tmpSource,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt,_tmpLanguage,_tmpAiModel,_tmpSummaryType,_tmpThumbnailUrl,_tmpPageCount,_tmpVideoId,_tmpChannelTitle,_tmpVideoDuration,_tmpViewCount,_tmpHasTranscript,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Card>> searchCards(final String query) {
    final String _sql = "SELECT * FROM cards WHERE title LIKE ? OR content LIKE ? OR summary LIKE ? OR source LIKE ? OR tags LIKE ? OR metadata LIKE ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    _argIndex = 4;
    _statement.bindString(_argIndex, query);
    _argIndex = 5;
    _statement.bindString(_argIndex, query);
    _argIndex = 6;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cards"}, new Callable<List<Card>>() {
      @Override
      @NonNull
      public List<Card> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfAiModel = CursorUtil.getColumnIndexOrThrow(_cursor, "aiModel");
          final int _cursorIndexOfSummaryType = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryType");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "pageCount");
          final int _cursorIndexOfVideoId = CursorUtil.getColumnIndexOrThrow(_cursor, "videoId");
          final int _cursorIndexOfChannelTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "channelTitle");
          final int _cursorIndexOfVideoDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "videoDuration");
          final int _cursorIndexOfViewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "viewCount");
          final int _cursorIndexOfHasTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "hasTranscript");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final List<Card> _result = new ArrayList<Card>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Card _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final CardType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toCardType(_tmp);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final List<String> _tmpTags;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp_1);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final String _tmpAiModel;
            _tmpAiModel = _cursor.getString(_cursorIndexOfAiModel);
            final String _tmpSummaryType;
            _tmpSummaryType = _cursor.getString(_cursorIndexOfSummaryType);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final Integer _tmpPageCount;
            if (_cursor.isNull(_cursorIndexOfPageCount)) {
              _tmpPageCount = null;
            } else {
              _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            }
            final String _tmpVideoId;
            if (_cursor.isNull(_cursorIndexOfVideoId)) {
              _tmpVideoId = null;
            } else {
              _tmpVideoId = _cursor.getString(_cursorIndexOfVideoId);
            }
            final String _tmpChannelTitle;
            if (_cursor.isNull(_cursorIndexOfChannelTitle)) {
              _tmpChannelTitle = null;
            } else {
              _tmpChannelTitle = _cursor.getString(_cursorIndexOfChannelTitle);
            }
            final String _tmpVideoDuration;
            if (_cursor.isNull(_cursorIndexOfVideoDuration)) {
              _tmpVideoDuration = null;
            } else {
              _tmpVideoDuration = _cursor.getString(_cursorIndexOfVideoDuration);
            }
            final String _tmpViewCount;
            if (_cursor.isNull(_cursorIndexOfViewCount)) {
              _tmpViewCount = null;
            } else {
              _tmpViewCount = _cursor.getString(_cursorIndexOfViewCount);
            }
            final boolean _tmpHasTranscript;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfHasTranscript);
            _tmpHasTranscript = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _item = new Card(_tmpId,_tmpTitle,_tmpContent,_tmpSummary,_tmpType,_tmpSource,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt,_tmpLanguage,_tmpAiModel,_tmpSummaryType,_tmpThumbnailUrl,_tmpPageCount,_tmpVideoId,_tmpChannelTitle,_tmpVideoDuration,_tmpViewCount,_tmpHasTranscript,_tmpMetadata);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CardSearchResult>> searchCardsWithMatchInfo(final String query) {
    final String _sql = "SELECT *, CASE   WHEN title LIKE ? THEN 'title'   WHEN content LIKE ? THEN 'content'   WHEN summary LIKE ? THEN 'summary'   WHEN source LIKE ? THEN 'source'   WHEN tags LIKE ? THEN 'tags'   WHEN metadata LIKE ? THEN 'metadata'   ELSE 'unknown' END AS matchedField FROM cards WHERE title LIKE ? OR content LIKE ? OR summary LIKE ? OR source LIKE ? OR tags LIKE ? OR metadata LIKE ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 12);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    _argIndex = 4;
    _statement.bindString(_argIndex, query);
    _argIndex = 5;
    _statement.bindString(_argIndex, query);
    _argIndex = 6;
    _statement.bindString(_argIndex, query);
    _argIndex = 7;
    _statement.bindString(_argIndex, query);
    _argIndex = 8;
    _statement.bindString(_argIndex, query);
    _argIndex = 9;
    _statement.bindString(_argIndex, query);
    _argIndex = 10;
    _statement.bindString(_argIndex, query);
    _argIndex = 11;
    _statement.bindString(_argIndex, query);
    _argIndex = 12;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cards"}, new Callable<List<CardSearchResult>>() {
      @Override
      @NonNull
      public List<CardSearchResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfAiModel = CursorUtil.getColumnIndexOrThrow(_cursor, "aiModel");
          final int _cursorIndexOfSummaryType = CursorUtil.getColumnIndexOrThrow(_cursor, "summaryType");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "pageCount");
          final int _cursorIndexOfVideoId = CursorUtil.getColumnIndexOrThrow(_cursor, "videoId");
          final int _cursorIndexOfChannelTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "channelTitle");
          final int _cursorIndexOfVideoDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "videoDuration");
          final int _cursorIndexOfViewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "viewCount");
          final int _cursorIndexOfHasTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "hasTranscript");
          final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
          final int _cursorIndexOfMatchedField = CursorUtil.getColumnIndexOrThrow(_cursor, "matchedField");
          final List<CardSearchResult> _result = new ArrayList<CardSearchResult>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CardSearchResult _item;
            final String _tmpMatchedField;
            _tmpMatchedField = _cursor.getString(_cursorIndexOfMatchedField);
            final Card _tmpCard;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final CardType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toCardType(_tmp);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final List<String> _tmpTags;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp_1);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final String _tmpAiModel;
            _tmpAiModel = _cursor.getString(_cursorIndexOfAiModel);
            final String _tmpSummaryType;
            _tmpSummaryType = _cursor.getString(_cursorIndexOfSummaryType);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final Integer _tmpPageCount;
            if (_cursor.isNull(_cursorIndexOfPageCount)) {
              _tmpPageCount = null;
            } else {
              _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            }
            final String _tmpVideoId;
            if (_cursor.isNull(_cursorIndexOfVideoId)) {
              _tmpVideoId = null;
            } else {
              _tmpVideoId = _cursor.getString(_cursorIndexOfVideoId);
            }
            final String _tmpChannelTitle;
            if (_cursor.isNull(_cursorIndexOfChannelTitle)) {
              _tmpChannelTitle = null;
            } else {
              _tmpChannelTitle = _cursor.getString(_cursorIndexOfChannelTitle);
            }
            final String _tmpVideoDuration;
            if (_cursor.isNull(_cursorIndexOfVideoDuration)) {
              _tmpVideoDuration = null;
            } else {
              _tmpVideoDuration = _cursor.getString(_cursorIndexOfVideoDuration);
            }
            final String _tmpViewCount;
            if (_cursor.isNull(_cursorIndexOfViewCount)) {
              _tmpViewCount = null;
            } else {
              _tmpViewCount = _cursor.getString(_cursorIndexOfViewCount);
            }
            final boolean _tmpHasTranscript;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfHasTranscript);
            _tmpHasTranscript = _tmp_2 != 0;
            final String _tmpMetadata;
            if (_cursor.isNull(_cursorIndexOfMetadata)) {
              _tmpMetadata = null;
            } else {
              _tmpMetadata = _cursor.getString(_cursorIndexOfMetadata);
            }
            _tmpCard = new Card(_tmpId,_tmpTitle,_tmpContent,_tmpSummary,_tmpType,_tmpSource,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt,_tmpLanguage,_tmpAiModel,_tmpSummaryType,_tmpThumbnailUrl,_tmpPageCount,_tmpVideoId,_tmpChannelTitle,_tmpVideoDuration,_tmpViewCount,_tmpHasTranscript,_tmpMetadata);
            _item = new CardSearchResult(_tmpCard,_tmpMatchedField);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Card>> getCardsByTags(final List<String> tags) {
    return CardDao.DefaultImpls.getCardsByTags(CardDao_Impl.this, tags);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
