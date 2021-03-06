#!/usr/bin/env dad --no-color --dry-run

(defn base-dir [x]
  (dad/render "/tmp/dad_test/{{x}}" {:x  x}))

(directory (base-dir "post") {:mode "755"})
(directory (base-dir "pre/a") {:action :delete})
(directory (base-dir "post") {:mode "744"})

(download {:path (base-dir "project.clj")
           :url "https://raw.githubusercontent.com/liquidz/dad/master/project.clj"
           :mode "755"})

(file (base-dir "foo"))
(file (base-dir "pre/dummy") {:action :delete})

(git {:url "https://github.com/liquidz/dad"
      :path (base-dir "dad")})

(execute {:command "touch hello"
          :cwd (base-dir "")})

(execute {:command "touch error"
          :cwd (base-dir "")
          :pre-not "test -e hello"})

(link (base-dir "world") {:to (base-dir "hello")})

(load-file "loaded_tasks.clj")
