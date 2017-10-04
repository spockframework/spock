var elementTagsStore = [[]]; // list of tag lists (one entry for each element's tag list); first list item reserved for elements with empty tag list
var distinctTags = {}; // multimap that maps from tag key to list of distinct tags w/ that key
var specs = [];
var reportModel;

var currentState = "all";
var currentTags = {}; // multimap that maps from tag keys to list of selected tag values
var currentKeywords = [];
var expandSearches = true;

var stopWords = [
  "the",
  "you",
  "she",
  "has",
  "have",
  "can",
  "may",
  "might",
  "shall",
  "should"
];

$(document).ready(function() {
  generateReportModel();
  configureTemplating();
  renderTemplate();

  drawPieCharts();
  computeInheritedTags();
  computeKeywords();
  filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);

  configureTreeElements();
  configureStateFiltering();
  configureViewSelection();
  configureTagFiltering();
  configureOptions();
  configureSearch();
  configureAttachmentViewers();
});

function drawPieCharts() {
  $("h1 .pie").peity("pie", {
    fill: ["#468847", "#B94A48", "#D3D3D3"],
    diameter: 39
  });
  $("h2 .pie").peity("pie", {
    fill: ["#468847", "#B94A48", "#D3D3D3"],
    diameter: 27
  });
  $("h3 .pie").peity("pie", {
    fill: ["#468847", "#B94A48", "#D3D3D3"],
    diameter: 25
  });
  $(".pieContainer").tooltip();
}

function computeInheritedTags() {
  $(".specElement").each(function() {
    var tagsIndex = $(this).data("tags");
    if (_.isUndefined(tagsIndex)) tagsIndex = 0;
    var elementTags = elementTagsStore[tagsIndex];
    var definedTags = _.object(_.map(elementTags, function(tag) {
      return [tag.key, tag.value];
    }));
    $(this).data("definedTags", definedTags);
    $(this).data("inheritedTags", _.extend({}, $(this).parent().closest(".specElement").data("inheritedTags"), definedTags));
  });
}

function computeKeywords() {
  $(".specElement").each(function() {
    var definedKeywords = extractKeywords($(this).find("> .elementHeader .elementName").text());
    $(this).data("definedKeywords", definedKeywords);
    var parentKeywords = $(this).parent().closest(".specElement").data("inherited-keywords");
    if (_.isUndefined(parentKeywords) || _.isNull(parentKeywords)) parentKeywords = [];
    $(this).data("inheritedKeywords", _.union(parentKeywords, definedKeywords));
  })
}

function filterSpecElementsByTagsAndStateAndKeywords(tags, state, keywords) {
  var tagCount = _.size(tags);
  var keywordCount = _.size(keywords);
  var specElements = $(".specElement");

  if (tagCount == 0 && keywordCount == 0 && state == "all") {
    specElements.show();
    if (expandSearches) {
      expandToFeatures();
    }
    return;
  }

  var elementsToShow = state == "all" ? specElements : $(".specElement." + state);

  if (tagCount > 0) {
    elementsToShow = elementsToShow.filter(function() {
      return elementTagsMatchSearchTags($(this).data("inheritedTags"), tags);
    });
  }

  if (keywordCount > 0) {
    elementsToShow = elementsToShow.filter(function() {
      return elementKeywordsMatchSearchKeywords($(this).data("inheritedKeywords"), keywords);
    });
  }

  specElements.hide();
  elementsToShow.show();
  elementsToShow.parents(".specElement").show();

  if (expandSearches) {
    expandSearchResults(tags, state, keywords, elementsToShow);
  }
}

function elementTagsMatchSearchTags(elementTags, searchTags) {
  return _.every(searchTags, function(allowedValues, key) {
    var actualValue = elementTags[key];
    return _.contains(allowedValues, actualValue);
  });
}

function elementKeywordsMatchSearchKeywords(elementKeywords, searchKeywords) {
  return _.every(searchKeywords, function(searchKeyword) {
    return _.some(elementKeywords, function(elementKeyword) {
      return elementKeyword.length >= searchKeyword.length
          && elementKeyword.substr(0, searchKeyword.length) == searchKeyword;
    });
  });
}

function expandSearchResults(tags, state, keywords, elementsToShow) {
  var elementsToExpandTo = elementsToShow.filter(function() {
    var element = $(this);
    return element.hasClass(state) || _.some(tags, function(allowedValues, key) {
      var tag = _.pick(tags, key);
      return elementTagsMatchSearchTags(element.data("definedTags"), tag);
    }) || _.some(keywords, function(keyword) {
      return elementKeywordsMatchSearchKeywords(element.data("definedKeywords"), [keyword]);
    });
  });

  collapseElements($(".specElement"));
  expandElements(elementsToExpandTo.parents(".specElement"));
}

function configureTreeElements() {
  $("a.elementName").click(function() {
    toggleElements($(this).closest(".element"));
  });
}

function configureStateFiltering() {
  $("#state-all").click(function() {
    currentState = "all";
    filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
  });
  $("#state-passed").click(function() {
    currentState = "passed";
    filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
  });
  $("#state-failed").click(function() {
    currentState = "failed";
    filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
  });
  $("#state-skipped").click(function() {
    currentState = "skipped";
    filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
  });
}

function configureViewSelection() {
  $("#view-packages").click(function() {
    collapseElements($(".element"));
  });

  $("#view-specifications").click(function() {
    expandElements($(".package"));
    collapseElements($(".element:not(.package)"));
  });

  $("#view-features").click(function() {
    expandToFeatures();
  });

  $("#view-feature-details").click(function() {
    expandElements($(".specElement"));
    collapseElements($(".element:not(.specElement)"));
  });

  $("#view-exceptions").click(function() {
    expandElements($(".specElement:has(.exceptions)"));
    expandElements($(".element.exceptions"));
    collapseElements($(".element:not(.exceptions):not(:has(.exceptions))"));
  });

  $("#view-full-details").click(function() {
    expandElements($(".element"));
  });
}

function configureTagFiltering() {
  var select = $("#tags");
  var index = 0
  _.each(distinctTags, function(tags, key) {
    _.each(tags, function(tag) {
      select.append($("<option/>").attr("value", index++).data("tag", tag).text(tag.name));
    });
  });
  select.multiselect({
    'buttonText': function() {
      return "Tags";
    },
    onChange: function(element, checked) {
      var tag = $(element).data("tag");
      if (checked) {
        addToMultimap(currentTags, tag.key, tag.value);
      } else {
        removeFromMultimap(currentTags, tag.key, tag.value);
      }
      filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
    }
  });
}

function configureOptions() {
  $("#options").multiselect({
    'buttonText': function() {
      return "Options"
    },
    onChange: function(element, checked) {
      switch(element.attr("value")) {
      case "showNarrative":
        $(".narrative").toggle();
        break;
      case "showResults":
        $(".results").toggle();
        break;
      case "expandSearches":
        expandSearches = checked;
        if (checked) {
          filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
        }
        break;
      default:
        console.log("unknown option: " + element.id);
      }
    }
  });
}

function configureSearch() {
  $("#searchTerms").keyup(function() {
    currentKeywords = extractKeywords($(this).val());
    filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
  });
}

function configureAttachmentViewers() {
  var idx = 0;
  $(".attachments .elementBody").each(function() {
    $(this).find("a").colorbox({iframe: true, current: "Attachment {current} of {total}",
      transition: "none", width: "90%", height: "90%", scalePhotos: false, rel: "atts-" + idx++});
  });
}

function configureTemplating() {
  Handlebars.registerHelper("pieCounts", function(counts) {
    return [counts.passed, counts.failed, counts.skipped].join(",");
  });
  Handlebars.registerHelper("resultCounts", function(counts, name) {
    var total = counts.passed + counts.failed + counts.skipped;
    var result = getCountedNoun(total, "spec") + " total";

    if (counts.passed > 0) {
      result += ", " + counts.passed + " passed";
    }
    if (counts.failed > 0) {
      result += ", " + counts.failed + " failed";
    }
    if (counts.skipped > 0) {
      result += ", " + counts.skipped + " skipped";
    }

    return result;
  });
  Handlebars.registerHelper("narrative", function(narrative) {
    var lines = narrative.split("\n");
    var escapedLines = _.map(lines, function(line) {
      return Handlebars.Utils.escapeExpression(line);
    });
    return new Handlebars.SafeString(escapedLines.join("<br/>"));
  });
  Handlebars.registerHelper("tagsIndex", function(tags) {
    if (_.isUndefined(tags)) return 0;

    var index = _.size(elementTagsStore);
    elementTagsStore.push(tags);
    return index;
  });
  Handlebars.registerHelper("resultLabel", function(result) {
    return result == "passed" ? "label-success" : result == "failed" ? "label-important" : "label-warning";
  });
  Handlebars.registerHelper("resultIcon", function(result) {
    //return result == "passed" ? "✓" : result == "failed" ? "✗" : "?";
    //return result == "passed" ? "" : result == "failed" ? "Failed" : "Skipped";
    return "";
  });
  Handlebars.registerHelper("iterate", function(object, options) {
    return _.map(object, function(value, key) {
      return options.fn({value: value, key: key});
    }).join("\n");
  });
  Handlebars.registerHelper("duration", function(duration) {
    var date = new Date(duration);

    if (date.getUTCFullYear() > 1970 || date.getUTCMonth() > 0) {
      return "(> 31 days, seriously?!)";
    }

    var result = "(";

    var days = date.getUTCDate() - 1;
    if (days > 0) {
      result += days + "d:";
    }

    var hours = date.getUTCHours();
    if (days > 0 || hours > 0) {
      result += hours + "h:";
    }

    var minutes = date.getUTCMinutes();
    if (days > 0 || hours > 0 || minutes > 0) {
      result += minutes + "m:";
    }

    var seconds = date.getUTCSeconds();
    var millis = date.getUTCMilliseconds();
    if (days > 0 || hours > 0 || minutes > 0 || duration >= 5000 || duration < 10) {
      result += Math.round(seconds + (millis / 1000)) + "s"
    } else {
      //result += millis + "ms";
      result += (seconds + millis / 1000).toFixed(2) + "s";
    }

    return result + ")";
  });
}

function getTagName(key, value) {
  return value === true ? key : key + "-" + value;
}

function getCountedNoun(count, noun) {
  return count + " " + noun + (count == 1 ? "" : "s");
}

function renderTemplate() {
  var template = Handlebars.compile($("#template").html());
  $("#template-placeholder").html(template(reportModel));
}

function loadLogFile(logFile) {
  specs = specs.concat(logFile);
}

function generateReportModel() {
  _.each(specs, function(spec) {
    spec.startTime = new Date(spec.start).getTime();
    spec.endTime = new Date(spec.end).getTime();
    spec.duration = spec.endTime - spec.startTime;
    spec.featureCount = _.size(spec.features);
    spec.counts = getResultCounts(spec.features);
    _.each(spec.tags, function(tag) {
      if (_.has(tag, "key")) {
        addToMultimap(distinctTags, tag.key, tag)
      }
    });
    _.each(spec.features, function(feature) {
      feature.startTime = new Date(feature.start).getTime();
      feature.endTime = new Date(feature.end).getTime();
      feature.duration = feature.endTime - feature.startTime;
      _.each(feature.tags, function(tag) {
        if (_.has(tag, "key")) {
          addToMultimap(distinctTags, tag.key, tag)
        }
      });
      _.each(feature.iterations, function(iteration) {
          iteration.startTime = new Date(iteration.start).getTime();
          iteration.endTime = new Date(iteration.end).getTime();
          iteration.duration = iteration.endTime - iteration.startTime;

      })
    })
  });
  var specsByPackage = _.groupBy(specs, "package");
  var packages = _.chain(specsByPackage)
      .map(function(specs, pkg) {
        return {name: pkg, duration: getTotalDuration(specs), specCount: _.size(specs),
          featureCount: countFeatures(specs), counts: getResultCounts(specs), specs: specs};
      })
      .sortBy("name")
      .value();
  reportModel = {duration: getTotalDuration(specs), packageCount: _.size(packages), specCount: _.size(specs),
    featureCount: countFeatures(specs), counts: getResultCounts(specs), packages: packages};
}

function countFeatures(specs) {
  return _.chain(specs)
      .map(function(spec) { return _.size(spec.features); })
      .reduce(function(total, size) { return total + size; }, 0)
      .value();
}

function addToMultimap(multimap, key, value) {
  if (_.has(multimap, key)) {
    var values = multimap[key];
    var contained = _.some(values, function(v){
      return _.isEqual(v, value);
    });
    if (!contained) {
      values.push(value);
    }
  } else {
    multimap[key] = [value];
  }
}

function addAllToMultimap(multimap, map) {
  _.each(map, function(value, key) {
    addToMultimap(multimap, key, value);
  });
}

function removeFromMultimap(multimap, key, value) {
  if (_.has(multimap, key)) {
    var values = multimap[key];
    var contained = _.some(values, function(v){
      return _.isEqual(v, value);
    });
    if (contained) {
      if (_.size(values) == 1) {
        delete multimap[key];
      } else {
        multimap[key] = _.without(values, value);
      }
    }
  }
}

function removeAllFromMultimap(multimap, map) {
  _.each(map, function(value, key) {
    removeFromMultimap(multimap, key, value);
  });
}

function getTotalDuration(specs) {
  var sortedSpecs = _.sortBy(specs, "startTime");
  var last = _.last(sortedSpecs);
  var result = _.reduce(_.zip(_.initial(sortedSpecs), _.rest(sortedSpecs)), function(memo, pair) {
    return memo + Math.min(pair[0].endTime, pair[1].startTime) - pair[0].startTime
  }, last.endTime - last.startTime);
  return result;
}

function getResultCounts(elements) {
  var counts = _.defaults(_.countBy(elements, "result"), {passed: 0, failed: 0, skipped: 0});
  return _.extend(counts, {total: counts.passed + counts.failed + counts.skipped})
}

function extractKeywords(text) {
  if (_.isUndefined(text)) return [];
  var words = text.split(/\W+/);
  return _.chain(words).map(function(word) {
    return word.toLowerCase();
  }).uniq().filter(function(word) {
    return word.length > 2 && !_.contains(stopWords, word);
  }).value();
}

function expandToFeatures() {
  expandElements($(".package, .spec"));
  collapseElements($(".element:not(.package):not(.spec)"));
}

function expandElements(elements) {
  elements.removeClass("collapsed");
  elements.children(".elementBody").show();
}

function collapseElements(elements) {
  elements.addClass("collapsed");
  elements.children(".elementBody").hide();
}

function toggleElements(elements) {
  elements.toggleClass("collapsed");
  elements.children(".elementBody").toggle();
}


