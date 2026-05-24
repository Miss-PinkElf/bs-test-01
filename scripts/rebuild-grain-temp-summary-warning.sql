START TRANSACTION;

UPDATE grain_temp_summary
SET warning_level = CASE
        WHEN max_temp >= 28.00 THEN 'WARNING'
        WHEN max_temp >= 25.00 THEN 'ATTENTION'
        ELSE 'NORMAL'
    END,
    warning_flag = CASE
        WHEN max_temp >= 25.00 THEN 1
        ELSE 0
    END,
    warning_message = CASE
        WHEN max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN max_temp >= 25.00 THEN '最高粮温接近阈值，建议持续关注'
        ELSE NULL
    END,
    analysis_result = CASE
        WHEN max_temp >= 25.00 THEN '粮温关注'
        ELSE '粮温正常'
    END,
    analysis_remark = CASE
        WHEN max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN max_temp >= 25.00 THEN '粮温接近阈值'
        ELSE '粮温整体平稳'
    END;

COMMIT;

SELECT
    COUNT(*) AS mismatch_count_after_rebuild
FROM grain_temp_summary
WHERE warning_level <> CASE
        WHEN max_temp >= 28.00 THEN 'WARNING'
        WHEN max_temp >= 25.00 THEN 'ATTENTION'
        ELSE 'NORMAL'
    END
    OR warning_flag <> CASE
        WHEN max_temp >= 25.00 THEN 1
        ELSE 0
    END
    OR COALESCE(warning_message, '') <> CASE
        WHEN max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN max_temp >= 25.00 THEN '最高粮温接近阈值，建议持续关注'
        ELSE ''
    END
    OR COALESCE(analysis_result, '') <> CASE
        WHEN max_temp >= 25.00 THEN '粮温关注'
        ELSE '粮温正常'
    END
    OR COALESCE(analysis_remark, '') <> CASE
        WHEN max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN max_temp >= 25.00 THEN '粮温接近阈值'
        ELSE '粮温整体平稳'
    END;

SELECT
    id,
    warehouse_id,
    collected_at,
    max_temp,
    warning_level,
    warning_flag,
    warning_message,
    analysis_result,
    analysis_remark
FROM grain_temp_summary
ORDER BY collected_at DESC, id DESC
LIMIT 30;
