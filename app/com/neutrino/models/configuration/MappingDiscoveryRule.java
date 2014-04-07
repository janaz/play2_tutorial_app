package com.neutrino.models.configuration;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.neutrino.csv.parsers.DateTimeParser;
import org.pojava.datetime.DateTime;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "MappingDiscoveryRule")
public class MappingDiscoveryRule extends Model {

    @Id
    @Column(name = "ID")
    public Integer id;

    @Column(name = "CoreTable", length = 64)
    public String coreTable;

    @Column(name = "CoreColumn", length = 64)
    public String coreColumn;

    @Column(name = "CoreType", length = 30)
    public String coreType;

    @Column(name = "MandatoryFlag")
    public Boolean mandatoryFlag;

    @Column(name = "RefCode", length = 30)
    public String refCode;

    @Column(name = "RefFullScore")
    public Integer refFullScore;

    @Column(name = "RefMinimumPercMatch", precision = 5, scale = 2)
    public BigDecimal refMinimumPercMatch;

    @Column(name = "RefFullScorePercThresh", precision = 5, scale = 2)
    public BigDecimal refFullScorePercThresh;

    @Column(name = "NullMinimumPerc", precision = 5, scale = 2)
    public BigDecimal nullMinimumPerc;

    @Column(name = "NullMaximumPerc", precision = 5, scale = 2)
    public BigDecimal nullMaximumPerc;

    @Column(name = "NullPenalty")
    public Integer nullPenalty;

    @Column(name = "UniqueMinimumPerc", precision = 5, scale = 2)
    public BigDecimal uniqueMinimumPerc;

    @Column(name = "UniqueMaximumPerc", precision = 5, scale = 2)
    public BigDecimal uniqueMaximumPerc;

    @Column(name = "uniqueScore")
    public Integer uniqueScore;

    @Column(name = "NamePatterns", length = 256)
    public String namePatterns;

    @Column(name = "NameScore")
    public Integer nameScore;

    @Column(name = "MinimumValue", length = 30)
    public String minimumValue;

    @Column(name = "MaximumValue", length = 30)
    public String maximumValue;

    @Column(name = "AllowedExcPerc", precision = 5, scale = 2)
    public BigDecimal allowedExcPerc;

    @Column(name = "ValScore")
    public Integer valScore;

    @Column(name = "valPenalty")
    public Integer valPenalty;

    @Column(name = "FormatsCode", length = 30)
    public String formatsCode;

    @Column(name = "FormatsMinimumPercThresh", precision = 5, scale = 2)
    public BigDecimal formatsMinimumPercThresh;

    @Column(name = "FormatsMetScore")
    public Integer formatsMetScore;

    @Column(name = "RegexPattern", length = 256)
    public String regexPattern;

    @Column(name = "RegexMinimumPercThresh", precision = 5, scale = 2)
    public BigDecimal regexMinimumPercThresh;

    @Column(name = "RegexMetScore")
    public Integer regexMetScore;

    @Column(name = "DataTypes", length = 30)
    public String dataTypes;

    @Column(name = "MaybePointsThresh")
    public Integer maybePointsThresh;

    @Column(name = "ConfPointsThresh")
    public Integer confPointsThresh;

    @Column(name = "CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name = "ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static final Finder<Integer, MappingDiscoveryRule> find = new Finder<Integer, MappingDiscoveryRule>(
            Integer.class, MappingDiscoveryRule.class);

    @Transient
    private String _compiledFor_namePatterns;
    @Transient
    private List<Pattern> _namePatternsCompiled;

    @Transient
    private long minValueInt;
    @Transient
    private long maxValueInt;
    @Transient
    private Boolean minMaxIsInt;
    @Transient
    private Boolean minMaxIsDate;
    @Transient
    private DateTime minValueDate;
    @Transient
    private DateTime maxValueDate;

    public Pattern regexPatternCompiled() {
        if (regexPattern == null || regexPattern.trim().isEmpty()) {
            return null;
        }
        return Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
    }

    private List<Pattern> namePatternsCompiled() {
        if (namePatterns == null || namePatterns.trim().isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        if (_compiledFor_namePatterns == null || !_compiledFor_namePatterns.equals(namePatterns)) {
            _compiledFor_namePatterns = namePatterns;
            List<String> tokens = Arrays.asList(namePatterns.split(","));
            _namePatternsCompiled = Lists.newArrayList(Lists.transform(tokens, new Function<String, Pattern>() {
                @Nullable
                @Override
                public Pattern apply(@Nullable String s) {
                    String patternStr = s.trim().replaceAll("%", ".*?");
                    return Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                }
            }));

        }
        return _namePatternsCompiled;
    }

    public boolean withinMinMax(String value) {
        if (minMaxIsInt == null) {
            minMaxIsInt = true;
            try {
                minValueInt = Long.parseLong(minimumValue);
                maxValueInt = Long.parseLong(maximumValue);
            } catch (Exception e) {
                minMaxIsInt = false;
            }
        }
        if (minMaxIsDate == null) {
            minValueDate = DateTimeParser.instance().parse(minimumValue);
            maxValueDate = DateTimeParser.instance().parse(minimumValue);
            minMaxIsDate = (minValueDate != null) && (maxValueDate != null);
        }
        if (minMaxIsInt.booleanValue()) {
            try {
                long valueInt = Long.parseLong(value);
                return valueInt >= minValueInt && valueInt <= maxValueInt;
            } catch (Exception e) {
                return false;
            }
        } else if (minMaxIsDate.booleanValue()) {
            DateTime valueDate = DateTimeParser.instance().parse(value);
            return valueDate.compareTo(minValueDate) >= 0 &&
                    valueDate.compareTo(maxValueDate) <= 0;
        } else {
            return value.compareTo(minimumValue) >= 0 &&
                    value.compareTo(maximumValue) <= 0;
        }
    }

    public boolean isNameMatching(String name) {
        for (Pattern p : namePatternsCompiled()) {
            if (p.matcher(name).find()) {
                return true;
            }
        }
        return false;
    }
}
