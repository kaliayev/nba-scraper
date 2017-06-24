(ns scrappy-ball.utils
  (:import [java.net URL]))

(def positions
  ["PG" "SG" "SF" "PF" "C"])

(def descriptor-columns
  #{"player" "pos" "g" "mp" "mp_per_g"})

(def fantasy-stats-totals
  #{"player" "pos" "g" "mp"
    "pts" "trb" "ast" "stl" "blk" "tov" "fg3"
    "ft" "fta"
    "fg" "fga"})

(def fantasy-stats-per-game
  #{"player" "pos" "g" "mp_per_g"
    "pts_per_g" "trb_per_g" "ast_per_g" "stl_per_g" "blk_per_g" "tov_per_g" "fg3_per_g"
    "ft_per_g" "fta_per_g"
    "fg_per_g" "fga_per_g"})

(def fantasy-stats-per-36
  #{"player" "pos" "g" "mp"
    "pts_per_mp" "trb_per_mp" "ast_per_mp" "stl_per_mp" "blk_per_mp" "tov_per_mp" "fg3_per_mp"
    "ft_per_mp" "fta_per_mp"
    "fg_per_mp" "fga_per_mp"})

(defn ->url
  "stat-types that can be used: 'totals' or 'per_game'"
  [year stat-type]
  (URL. (format "http://www.basketball-reference.com/leagues/NBA_%s_%s.html#%s_stats::none" year stat-type stat-type)))
