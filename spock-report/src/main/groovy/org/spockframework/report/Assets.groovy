/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.report

class Assets {
  private final String location
  private final String mode

  Assets(boolean local, boolean debug) {
    location = local ? "local" : "remote"
    mode = debug ? "debug" : "normal"
  }

  List getCss() {
    CSS[location][mode]
  }

  List getImg() {
    IMG[location]
  }

  List getJs() {
    JS[location][mode]
  }

  private static CSS =
    [
        local: [
            normal: [
                "css/bootstrap.min.css",
                "css/bootstrap-responsive.min.css",
                "css/bootstrap-multiselect.css",
                "css/colorbox.css",
                "css/report.css"
            ],
            debug: [
                "css/bootstrap.css",
                "css/bootstrap-responsive.css",
                "css/bootstrap-multiselect.css",
                "css/colorbox.css",
                "css/report.css"
            ]
        ],
        remote: [
            normal: [
                "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.2/css/bootstrap.no-icons.min.css",
                "css/bootstrap-multiselect.css",
                "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/example3/colorbox.css",
                "css/report.css"
            ],
            debug: [
                "http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.2.2/css/bootstrap.css",
                "http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.2.2/css/bootstrap-responsive.css",
                "css/bootstrap-multiselect.css",
                "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/example3/colorbox.css",
                "css/report.css"
            ]
        ]
    ]

  private static IMG =
    [
        local: [
            "img/colorbox/border.png",
            "img/colorbox/controls.png",
            "img/colorbox/loading.gif",
            "img/colorbox/loading_background.png",
            "img/colorbox/overlay.png",
            "img/glyphicons-halflings-white.png",
            "img/glyphicons-halflings.png",
            "img/glyphicons_circle_minus_small.png",
            "img/glyphicons_circle_plus_small.png"
        ],
        remote: [
            "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/example3/images/border.png",
            "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/example3/images/controls.png",
            "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/example3/images/loading.gif",
            "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/example3/images/loading_background.png",
            "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/example3/images/overlay.png",
            "http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.2.2/img/glyphicons-halflings-white.png",
            "http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.2.2/img/glyphicons-halflings.png",
            "img/glyphicons_circle_minus_small.png",
            "img/glyphicons_circle_plus_small.png"
        ]
    ]

  private static JS =
    [
        local:  [
            normal: [
                "js/jquery.min.js",
                "js/jquery.peity.min.js",
                "js/jquery.colorbox-min.js",
                "js/bootstrap.min.js",
                "js/bootstrap-multiselect.js",
                "js/underscore-min.js",
                "js/handlebars.min.js",
                "js/report.js"
            ],
            debug: [
                "js/jquery.js",
                "js/jquery.peity.js",
                "js/jquery.colorbox.js",
                "js/bootstrap.js",
                "js/bootstrap-multiselect.js",
                "js/underscore.js",
                "js/handlebars.js",
                "js/report.js"
            ]
        ],
        remote: [
            normal: [
                "http://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js",
                "js/jquery.peity.min.js",
                "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/jquery.colorbox-min.js",
                "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.2/js/bootstrap.min.js",
                "js/bootstrap-multiselect.js",
                "http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.4.3/underscore-min.js",
                "http://cdnjs.cloudflare.com/ajax/libs/handlebars.js/1.0.rc.2/handlebars.min.js",
                "js/report.js"
            ],
            debug: [
                "http://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.js",
                "js/jquery.peity.js",
                "http://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.3.27/jquery.colorbox.js",
                "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.2/js/bootstrap.js",
                "js/bootstrap-multiselect.js",
                "http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.4.3/underscore.js",
                "http://cdnjs.cloudflare.com/ajax/libs/handlebars.js/1.0.rc.2/handlebars.js",
                "js/report.js"
            ]
        ]
    ]
}
