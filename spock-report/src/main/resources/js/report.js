var currentState = "all";
var currentTags = {};
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
  "should",
];

$(document).ready(function() {
  drawPieCharts();
  computeInheritedTags();
  computeKeywords();
  filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
  $(".stats").toggle();

  configureTreeElements();
  configureStateFiltering();
  configureViewSelection();
  configureTagFiltering();
  configureOptions();
  configureSearch();
});

function drawPieCharts() {
  $("h1 .pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 30
  });
  $("h2 .pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 25
  });
  $("h3 .pie").peity("pie", {
    colours: ['#468847', '#B94A48', '#F89406'],
    diameter: 20
   });
}

function computeInheritedTags() {
  $(".specElement").each(function() {
    var definedTags = parseTagsString($(this).data("tags"));
    $(this).data("definedTags", definedTags);
    $(this).data("inheritedTags", _.extend({}, $(this).parent().closest(".specElement").data("inheritedTags"), definedTags));
  });
}

function computeKeywords() {
  $(".specElement").each(function() {
    var definedKeywords = extractKeywords($(this).find(" > .elementHeader .elementName").text());
    $(this).data("definedKeywords", definedKeywords);
    var parentKeywords = $(this).parent().closest(".specElement").data("inherited-keywords");
    if (_.isUndefined(parentKeywords)) parentKeywords = [];
    $(this).data("inheritedKeywords", _.union(parentKeywords, definedKeywords));
  })
}

function parseTagsString(tagsString) {
  var tags = {};
  if (_.isUndefined(tagsString)) return tags;

  _.each(tagsString.split(","), function(tagString) {
    var keyValue = tagString.split("=");
    var key = keyValue[0];
    var value = keyValue[1];
    if (!_.has(tags, key)) tags[key] = [];
    tags[key].push(value);
  });

  return tags;
}

function filterSpecElementsByTagsAndStateAndKeywords(tags, state, keywords) {
  var tagCount = _.size(tags);
  var keywordCount = _.size(keywords);

  if (tagCount == 0 && keywordCount == 0 && state == "all") {
    $(".specElement").show();
    if (expandSearches) {
      expandToRequirements();
    }
    return;
  }

  var elementsToShow = state == "all" ? $(".specElement") : $(".specElement." + state);

  if (tagCount > 0) {
    elementsToShow = elementsToShow.filter(function() {
      return specElementMatchesSearchTags($(this), $(this).data("inheritedTags"), tags);
    });
  }

  if (keywordCount > 0) {
    elementsToShow = elementsToShow.filter(function() {
      return specElementMatchesSearchKeywords($(this), $(this).data("inheritedKeywords"), keywords);
    });
  }

  $(".specElement").hide();
  elementsToShow.show();
  elementsToShow.parents(".specElement").show();

  if (expandSearches) {
    expandSearchResults(tags, state, keywords, elementsToShow);
  }
}

function specElementMatchesSearchTags(element, elementTags, searchTags) {
  return _.every(searchTags, function(allowedValues, key) {
    var actualValues = elementTags[key];
    return !_.isEmpty(_.intersection(actualValues, allowedValues));
  });
}

function specElementMatchesSearchKeywords(element, elementKeywords, searchKeywords) {
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
      return specElementMatchesSearchTags(element, element.data("definedTags"), tag);
    }) || _.some(keywords, function(keyword) {
      return specElementMatchesSearchKeywords(element, element.data("definedKeywords"), [keyword]);
    });
  });

  collapseElements($(".specElement"));
  expandElements(elementsToExpandTo.parents(".specElement"));
}

function configureTreeElements() {
  $(".elementHeader").click(function() {
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

  $("#view-requirements").click(function() {
    expandToRequirements();
  });

  $("#view-requirement-details").click(function() {
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
  $("#tags").multiselect({
    'text': function() {
      return "Tags"
    },
    onchange: function(element, checked) {
      var tagString = element.attr("value");
      var keyValue = tagString.split("=");
      var key = keyValue[0];
      var value = keyValue[1];
      if (checked) {
        if (!_.has(currentTags, key)) currentTags[key] = [];
        currentTags[key].push(value);
      } else {
        currentTags[key] = _.without(currentTags[key], value);
        if (_.isEmpty(currentTags[key])) {
          currentTags = _.omit(currentTags, key);
        }
      }
      filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
    }
  });
}

function configureOptions() {
  $("#options").multiselect({
    'text': function() {
      return "Options"
    },
    onchange: function(element, checked) {
      switch(element.attr("value")) {
      case "showNarrative":
        $(".narrative").toggle();
        break;
      case "showResults":
        $(".results").toggle();
        break;
      case "showStatistics":
        $(".stats").toggle();
        break;
      case "expandSearches":
        expandSearches = checked;
        if (checked) {
          filterSpecElementsByTagsAndStateAndKeywords(currentTags, currentState, currentKeywords);
        }
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

function extractKeywords(text) {
  if (_.isUndefined(text)) return [];
  var words = text.split(/\W+/);
  return _.chain(words).map(function(word) {
    return word.toLowerCase();
  }).uniq().filter(function(word) {
    return word.length > 2 && !_.contains(stopWords, word);
  }).value();
}

function expandToRequirements() {
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


