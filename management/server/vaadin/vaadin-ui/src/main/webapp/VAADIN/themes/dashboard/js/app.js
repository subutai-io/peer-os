




<!DOCTYPE html>
<html class="   ">
  <head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# object: http://ogp.me/ns/object# article: http://ogp.me/ns/article# profile: http://ogp.me/ns/profile#">
    <meta charset='utf-8'>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    
    <title>automation/content/critical-css/app.js at master · ibrennan/automation · GitHub</title>
    <link rel="search" type="application/opensearchdescription+xml" href="/opensearch.xml" title="GitHub" />
    <link rel="fluid-icon" href="https://github.com/fluidicon.png" title="GitHub" />
    <link rel="apple-touch-icon" sizes="57x57" href="/apple-touch-icon-114.png" />
    <link rel="apple-touch-icon" sizes="114x114" href="/apple-touch-icon-114.png" />
    <link rel="apple-touch-icon" sizes="72x72" href="/apple-touch-icon-144.png" />
    <link rel="apple-touch-icon" sizes="144x144" href="/apple-touch-icon-144.png" />
    <meta property="fb:app_id" content="1401488693436528"/>

      <meta content="@github" name="twitter:site" /><meta content="summary" name="twitter:card" /><meta content="ibrennan/automation" name="twitter:title" /><meta content="automation - A collection of automated scripts for processing sites / content. Uses CasperJS." name="twitter:description" /><meta content="https://avatars0.githubusercontent.com/u/1170905?s=400" name="twitter:image:src" />
<meta content="GitHub" property="og:site_name" /><meta content="object" property="og:type" /><meta content="https://avatars0.githubusercontent.com/u/1170905?s=400" property="og:image" /><meta content="ibrennan/automation" property="og:title" /><meta content="https://github.com/ibrennan/automation" property="og:url" /><meta content="automation - A collection of automated scripts for processing sites / content. Uses CasperJS." property="og:description" />

    <link rel="assets" href="https://assets-cdn.github.com/">
    <link rel="conduit-xhr" href="https://ghconduit.com:25035">
    

    <meta name="msapplication-TileImage" content="/windows-tile.png" />
    <meta name="msapplication-TileColor" content="#ffffff" />
    <meta name="selected-link" value="repo_source" data-pjax-transient />
      <meta name="google-analytics" content="UA-3769691-2">

    <meta content="collector.githubapp.com" name="octolytics-host" /><meta content="collector-cdn.github.com" name="octolytics-script-host" /><meta content="github" name="octolytics-app-id" /><meta content="D47072CC:29D0:19F2820:5391D1C7" name="octolytics-dimension-request_id" />
    

    
    
    <link rel="icon" type="image/x-icon" href="https://assets-cdn.github.com/favicon.ico" />


    <meta content="authenticity_token" name="csrf-param" />
<meta content="PquRrrsuxO7kbT0Dq6dvzZcEp+J1W00UZ83dsFbio/yGr84/4f+zBTKEzpQ82/06NXG5dm9wqgAA3ZKOoOnWkw==" name="csrf-token" />

    <link href="https://assets-cdn.github.com/assets/github-1c68c1c3e080d22c2679b14e4c09c9af9999ae64.css" media="all" rel="stylesheet" type="text/css" />
    <link href="https://assets-cdn.github.com/assets/github2-8f544b90b7afbf2dfa2d8228f205484ba43457d2.css" media="all" rel="stylesheet" type="text/css" />
    


    <meta http-equiv="x-pjax-version" content="b616d85a7db41a9ee898dcaac2e1a5da">

      
  <meta name="description" content="automation - A collection of automated scripts for processing sites / content. Uses CasperJS." />

  <meta content="1170905" name="octolytics-dimension-user_id" /><meta content="ibrennan" name="octolytics-dimension-user_login" /><meta content="18331790" name="octolytics-dimension-repository_id" /><meta content="ibrennan/automation" name="octolytics-dimension-repository_nwo" /><meta content="true" name="octolytics-dimension-repository_public" /><meta content="false" name="octolytics-dimension-repository_is_fork" /><meta content="18331790" name="octolytics-dimension-repository_network_root_id" /><meta content="ibrennan/automation" name="octolytics-dimension-repository_network_root_nwo" />
  <link href="https://github.com/ibrennan/automation/commits/master.atom" rel="alternate" title="Recent Commits to automation:master" type="application/atom+xml" />

  </head>


  <body class="logged_out  env-production linux vis-public page-blob">
    <a href="#start-of-content" tabindex="1" class="accessibility-aid js-skip-to-content">Skip to content</a>
    <div class="wrapper">
      
      
      
      


      
      <div class="header header-logged-out">
  <div class="container clearfix">

    <a class="header-logo-wordmark" href="https://github.com/">
      <span class="mega-octicon octicon-logo-github"></span>
    </a>

    <div class="header-actions">
        <a class="button primary" href="/join">Sign up</a>
      <a class="button signin" href="/login?return_to=%2Fibrennan%2Fautomation%2Fblob%2Fmaster%2Fcontent%2Fcritical-css%2Fapp.js">Sign in</a>
    </div>

    <div class="command-bar js-command-bar  in-repository">

      <ul class="top-nav">
          <li class="explore"><a href="/explore">Explore</a></li>
        <li class="features"><a href="/features">Features</a></li>
          <li class="enterprise"><a href="https://enterprise.github.com/">Enterprise</a></li>
          <li class="blog"><a href="/blog">Blog</a></li>
      </ul>
        <form accept-charset="UTF-8" action="/search" class="command-bar-form" id="top_search_form" method="get">

<div class="commandbar">
  <span class="message"></span>
  <input type="text" data-hotkey="s, /" name="q" id="js-command-bar-field" placeholder="Search or type a command" tabindex="1" autocapitalize="off"
    
    
      data-repo="ibrennan/automation"
      data-branch="master"
      data-sha="cc75e441b36b3b917442c80fc0441b31dc1ae6b4"
  >
  <div class="display hidden"></div>
</div>

    <input type="hidden" name="nwo" value="ibrennan/automation" />

    <div class="select-menu js-menu-container js-select-menu search-context-select-menu">
      <span class="minibutton select-menu-button js-menu-target" role="button" aria-haspopup="true">
        <span class="js-select-button">This repository</span>
      </span>

      <div class="select-menu-modal-holder js-menu-content js-navigation-container" aria-hidden="true">
        <div class="select-menu-modal">

          <div class="select-menu-item js-navigation-item js-this-repository-navigation-item selected">
            <span class="select-menu-item-icon octicon octicon-check"></span>
            <input type="radio" class="js-search-this-repository" name="search_target" value="repository" checked="checked" />
            <div class="select-menu-item-text js-select-button-text">This repository</div>
          </div> <!-- /.select-menu-item -->

          <div class="select-menu-item js-navigation-item js-all-repositories-navigation-item">
            <span class="select-menu-item-icon octicon octicon-check"></span>
            <input type="radio" name="search_target" value="global" />
            <div class="select-menu-item-text js-select-button-text">All repositories</div>
          </div> <!-- /.select-menu-item -->

        </div>
      </div>
    </div>

  <span class="help tooltipped tooltipped-s" aria-label="Show command bar help">
    <span class="octicon octicon-question"></span>
  </span>


  <input type="hidden" name="ref" value="cmdform">

</form>
    </div>

  </div>
</div>



      <div id="start-of-content" class="accessibility-aid"></div>
          <div class="site" itemscope itemtype="http://schema.org/WebPage">
    <div id="js-flash-container">
      
    </div>
    <div class="pagehead repohead instapaper_ignore readability-menu">
      <div class="container">
        

<ul class="pagehead-actions">


  <li>
    <a href="/login?return_to=%2Fibrennan%2Fautomation"
    class="minibutton with-count star-button tooltipped tooltipped-n"
    aria-label="You must be signed in to star a repository" rel="nofollow">
    <span class="octicon octicon-star"></span>Star
  </a>

    <a class="social-count js-social-count" href="/ibrennan/automation/stargazers">
      10
    </a>

  </li>

    <li>
      <a href="/login?return_to=%2Fibrennan%2Fautomation"
        class="minibutton with-count js-toggler-target fork-button tooltipped tooltipped-n"
        aria-label="You must be signed in to fork a repository" rel="nofollow">
        <span class="octicon octicon-repo-forked"></span>Fork
      </a>
      <a href="/ibrennan/automation/network" class="social-count">
        1
      </a>
    </li>
</ul>

        <h1 itemscope itemtype="http://data-vocabulary.org/Breadcrumb" class="entry-title public">
          <span class="repo-label"><span>public</span></span>
          <span class="mega-octicon octicon-repo"></span>
          <span class="author"><a href="/ibrennan" class="url fn" itemprop="url" rel="author"><span itemprop="title">ibrennan</span></a></span><!--
       --><span class="path-divider">/</span><!--
       --><strong><a href="/ibrennan/automation" class="js-current-repository js-repo-home-link">automation</a></strong>

          <span class="page-context-loader">
            <img alt="" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
          </span>

        </h1>
      </div><!-- /.container -->
    </div><!-- /.repohead -->

    <div class="container">
      <div class="repository-with-sidebar repo-container new-discussion-timeline js-new-discussion-timeline  ">
        <div class="repository-sidebar clearfix">
            

<div class="sunken-menu vertical-right repo-nav js-repo-nav js-repository-container-pjax js-octicon-loaders">
  <div class="sunken-menu-contents">
    <ul class="sunken-menu-group">
      <li class="tooltipped tooltipped-w" aria-label="Code">
        <a href="/ibrennan/automation" aria-label="Code" class="selected js-selected-navigation-item sunken-menu-item" data-hotkey="g c" data-pjax="true" data-selected-links="repo_source repo_downloads repo_commits repo_releases repo_tags repo_branches /ibrennan/automation">
          <span class="octicon octicon-code"></span> <span class="full-word">Code</span>
          <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>      </li>

        <li class="tooltipped tooltipped-w" aria-label="Issues">
          <a href="/ibrennan/automation/issues" aria-label="Issues" class="js-selected-navigation-item sunken-menu-item js-disable-pjax" data-hotkey="g i" data-selected-links="repo_issues /ibrennan/automation/issues">
            <span class="octicon octicon-issue-opened"></span> <span class="full-word">Issues</span>
            <span class='counter'>0</span>
            <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>        </li>

      <li class="tooltipped tooltipped-w" aria-label="Pull Requests">
        <a href="/ibrennan/automation/pulls" aria-label="Pull Requests" class="js-selected-navigation-item sunken-menu-item js-disable-pjax" data-hotkey="g p" data-selected-links="repo_pulls /ibrennan/automation/pulls">
            <span class="octicon octicon-git-pull-request"></span> <span class="full-word">Pull Requests</span>
            <span class='counter'>0</span>
            <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>      </li>


    </ul>
    <div class="sunken-menu-separator"></div>
    <ul class="sunken-menu-group">

      <li class="tooltipped tooltipped-w" aria-label="Pulse">
        <a href="/ibrennan/automation/pulse" aria-label="Pulse" class="js-selected-navigation-item sunken-menu-item" data-pjax="true" data-selected-links="pulse /ibrennan/automation/pulse">
          <span class="octicon octicon-pulse"></span> <span class="full-word">Pulse</span>
          <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>      </li>

      <li class="tooltipped tooltipped-w" aria-label="Graphs">
        <a href="/ibrennan/automation/graphs" aria-label="Graphs" class="js-selected-navigation-item sunken-menu-item" data-pjax="true" data-selected-links="repo_graphs repo_contributors /ibrennan/automation/graphs">
          <span class="octicon octicon-graph"></span> <span class="full-word">Graphs</span>
          <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>      </li>

      <li class="tooltipped tooltipped-w" aria-label="Network">
        <a href="/ibrennan/automation/network" aria-label="Network" class="js-selected-navigation-item sunken-menu-item js-disable-pjax" data-selected-links="repo_network /ibrennan/automation/network">
          <span class="octicon octicon-repo-forked"></span> <span class="full-word">Network</span>
          <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>      </li>
    </ul>


  </div>
</div>

              <div class="only-with-full-nav">
                

  

<div class="clone-url open"
  data-protocol-type="http"
  data-url="/users/set_protocol?protocol_selector=http&amp;protocol_type=clone">
  <h3><strong>HTTPS</strong> clone URL</h3>
  <div class="clone-url-box">
    <input type="text" class="clone js-url-field"
           value="https://github.com/ibrennan/automation.git" readonly="readonly">
    <span class="url-box-clippy">
    <button aria-label="copy to clipboard" class="js-zeroclipboard minibutton zeroclipboard-button" data-clipboard-text="https://github.com/ibrennan/automation.git" data-copied-hint="copied!" type="button"><span class="octicon octicon-clippy"></span></button>
    </span>
  </div>
</div>

  

<div class="clone-url "
  data-protocol-type="subversion"
  data-url="/users/set_protocol?protocol_selector=subversion&amp;protocol_type=clone">
  <h3><strong>Subversion</strong> checkout URL</h3>
  <div class="clone-url-box">
    <input type="text" class="clone js-url-field"
           value="https://github.com/ibrennan/automation" readonly="readonly">
    <span class="url-box-clippy">
    <button aria-label="copy to clipboard" class="js-zeroclipboard minibutton zeroclipboard-button" data-clipboard-text="https://github.com/ibrennan/automation" data-copied-hint="copied!" type="button"><span class="octicon octicon-clippy"></span></button>
    </span>
  </div>
</div>


<p class="clone-options">You can clone with
      <a href="#" class="js-clone-selector" data-protocol="http">HTTPS</a>
      or <a href="#" class="js-clone-selector" data-protocol="subversion">Subversion</a>.
  <span class="help tooltipped tooltipped-n" aria-label="Get help on which URL is right for you.">
    <a href="https://help.github.com/articles/which-remote-url-should-i-use">
    <span class="octicon octicon-question"></span>
    </a>
  </span>
</p>



                <a href="/ibrennan/automation/archive/master.zip"
                   class="minibutton sidebar-button"
                   aria-label="Download ibrennan/automation as a zip file"
                   title="Download ibrennan/automation as a zip file"
                   rel="nofollow">
                  <span class="octicon octicon-cloud-download"></span>
                  Download ZIP
                </a>
              </div>
        </div><!-- /.repository-sidebar -->

        <div id="js-repo-pjax-container" class="repository-content context-loader-container" data-pjax-container>
          


<a href="/ibrennan/automation/blob/a76b3a781ba47011ef5408122a7f3e81afd36fa7/content/critical-css/app.js" class="hidden js-permalink-shortcut" data-hotkey="y">Permalink</a>

<!-- blob contrib key: blob_contributors:v21:1781d0514e87aca03ed1793abee9067f -->

<p title="This is a placeholder element" class="js-history-link-replace hidden"></p>

<a href="/ibrennan/automation/find/master" data-pjax data-hotkey="t" class="js-show-file-finder" style="display:none">Show File Finder</a>

<div class="file-navigation">
  

<div class="select-menu js-menu-container js-select-menu" >
  <span class="minibutton select-menu-button js-menu-target" data-hotkey="w"
    data-master-branch="master"
    data-ref="master"
    role="button" aria-label="Switch branches or tags" tabindex="0" aria-haspopup="true">
    <span class="octicon octicon-git-branch"></span>
    <i>branch:</i>
    <span class="js-select-button">master</span>
  </span>

  <div class="select-menu-modal-holder js-menu-content js-navigation-container" data-pjax aria-hidden="true">

    <div class="select-menu-modal">
      <div class="select-menu-header">
        <span class="select-menu-title">Switch branches/tags</span>
        <span class="octicon octicon-x js-menu-close"></span>
      </div> <!-- /.select-menu-header -->

      <div class="select-menu-filters">
        <div class="select-menu-text-filter">
          <input type="text" aria-label="Filter branches/tags" id="context-commitish-filter-field" class="js-filterable-field js-navigation-enable" placeholder="Filter branches/tags">
        </div>
        <div class="select-menu-tabs">
          <ul>
            <li class="select-menu-tab">
              <a href="#" data-tab-filter="branches" class="js-select-menu-tab">Branches</a>
            </li>
            <li class="select-menu-tab">
              <a href="#" data-tab-filter="tags" class="js-select-menu-tab">Tags</a>
            </li>
          </ul>
        </div><!-- /.select-menu-tabs -->
      </div><!-- /.select-menu-filters -->

      <div class="select-menu-list select-menu-tab-bucket js-select-menu-tab-bucket" data-tab-filter="branches">

        <div data-filterable-for="context-commitish-filter-field" data-filterable-type="substring">


            <div class="select-menu-item js-navigation-item selected">
              <span class="select-menu-item-icon octicon octicon-check"></span>
              <a href="/ibrennan/automation/blob/master/content/critical-css/app.js"
                 data-name="master"
                 data-skip-pjax="true"
                 rel="nofollow"
                 class="js-navigation-open select-menu-item-text js-select-button-text css-truncate-target"
                 title="master">master</a>
            </div> <!-- /.select-menu-item -->
        </div>

          <div class="select-menu-no-results">Nothing to show</div>
      </div> <!-- /.select-menu-list -->

      <div class="select-menu-list select-menu-tab-bucket js-select-menu-tab-bucket" data-tab-filter="tags">
        <div data-filterable-for="context-commitish-filter-field" data-filterable-type="substring">


        </div>

        <div class="select-menu-no-results">Nothing to show</div>
      </div> <!-- /.select-menu-list -->

    </div> <!-- /.select-menu-modal -->
  </div> <!-- /.select-menu-modal-holder -->
</div> <!-- /.select-menu -->

  <div class="breadcrumb">
    <span class='repo-root js-repo-root'><span itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb"><a href="/ibrennan/automation" data-branch="master" data-direction="back" data-pjax="true" itemscope="url"><span itemprop="title">automation</span></a></span></span><span class="separator"> / </span><span itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb"><a href="/ibrennan/automation/tree/master/content" data-branch="master" data-direction="back" data-pjax="true" itemscope="url"><span itemprop="title">content</span></a></span><span class="separator"> / </span><span itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb"><a href="/ibrennan/automation/tree/master/content/critical-css" data-branch="master" data-direction="back" data-pjax="true" itemscope="url"><span itemprop="title">critical-css</span></a></span><span class="separator"> / </span><strong class="final-path">app.js</strong> <button aria-label="copy to clipboard" class="js-zeroclipboard minibutton zeroclipboard-button" data-clipboard-text="content/critical-css/app.js" data-copied-hint="copied!" type="button"><span class="octicon octicon-clippy"></span></button>
  </div>
</div>


  <div class="commit commit-loader file-history-tease js-deferred-content" data-url="/ibrennan/automation/contributors/master/content/critical-css/app.js">
    Fetching contributors…

    <div class="participation">
      <p class="loader-loading"><img alt="" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32-EAF2F5.gif" width="16" /></p>
      <p class="loader-error">Cannot retrieve contributors at this time</p>
    </div>
  </div>

<div class="file-box">
  <div class="file">
    <div class="meta clearfix">
      <div class="info file-name">
        <span class="icon"><b class="octicon octicon-file-text"></b></span>
        <span class="mode" title="File Mode">file</span>
        <span class="meta-divider"></span>
          <span>184 lines (132 sloc)</span>
          <span class="meta-divider"></span>
        <span>5.026 kb</span>
      </div>
      <div class="actions">
        <div class="button-group">
              <a class="minibutton disabled tooltipped tooltipped-w" href="#"
                 aria-label="You must be signed in to make or propose changes">Edit</a>
          <a href="/ibrennan/automation/raw/master/content/critical-css/app.js" class="button minibutton " id="raw-url">Raw</a>
            <a href="/ibrennan/automation/blame/master/content/critical-css/app.js" class="button minibutton js-update-url-with-hash">Blame</a>
          <a href="/ibrennan/automation/commits/master/content/critical-css/app.js" class="button minibutton " rel="nofollow">History</a>
        </div><!-- /.button-group -->
          <a class="minibutton danger disabled empty-icon tooltipped tooltipped-w" href="#"
             aria-label="You must be signed in to make or propose changes">
          Delete
        </a>
      </div><!-- /.actions -->
    </div>
      
  <div class="blob-wrapper data type-javascript js-blob-data">
       <table class="file-code file-diff tab-size-8">
         <tr class="file-code-line">
           <td class="blob-line-nums">
             <span id="L1" rel="#L1">1</span>
<span id="L2" rel="#L2">2</span>
<span id="L3" rel="#L3">3</span>
<span id="L4" rel="#L4">4</span>
<span id="L5" rel="#L5">5</span>
<span id="L6" rel="#L6">6</span>
<span id="L7" rel="#L7">7</span>
<span id="L8" rel="#L8">8</span>
<span id="L9" rel="#L9">9</span>
<span id="L10" rel="#L10">10</span>
<span id="L11" rel="#L11">11</span>
<span id="L12" rel="#L12">12</span>
<span id="L13" rel="#L13">13</span>
<span id="L14" rel="#L14">14</span>
<span id="L15" rel="#L15">15</span>
<span id="L16" rel="#L16">16</span>
<span id="L17" rel="#L17">17</span>
<span id="L18" rel="#L18">18</span>
<span id="L19" rel="#L19">19</span>
<span id="L20" rel="#L20">20</span>
<span id="L21" rel="#L21">21</span>
<span id="L22" rel="#L22">22</span>
<span id="L23" rel="#L23">23</span>
<span id="L24" rel="#L24">24</span>
<span id="L25" rel="#L25">25</span>
<span id="L26" rel="#L26">26</span>
<span id="L27" rel="#L27">27</span>
<span id="L28" rel="#L28">28</span>
<span id="L29" rel="#L29">29</span>
<span id="L30" rel="#L30">30</span>
<span id="L31" rel="#L31">31</span>
<span id="L32" rel="#L32">32</span>
<span id="L33" rel="#L33">33</span>
<span id="L34" rel="#L34">34</span>
<span id="L35" rel="#L35">35</span>
<span id="L36" rel="#L36">36</span>
<span id="L37" rel="#L37">37</span>
<span id="L38" rel="#L38">38</span>
<span id="L39" rel="#L39">39</span>
<span id="L40" rel="#L40">40</span>
<span id="L41" rel="#L41">41</span>
<span id="L42" rel="#L42">42</span>
<span id="L43" rel="#L43">43</span>
<span id="L44" rel="#L44">44</span>
<span id="L45" rel="#L45">45</span>
<span id="L46" rel="#L46">46</span>
<span id="L47" rel="#L47">47</span>
<span id="L48" rel="#L48">48</span>
<span id="L49" rel="#L49">49</span>
<span id="L50" rel="#L50">50</span>
<span id="L51" rel="#L51">51</span>
<span id="L52" rel="#L52">52</span>
<span id="L53" rel="#L53">53</span>
<span id="L54" rel="#L54">54</span>
<span id="L55" rel="#L55">55</span>
<span id="L56" rel="#L56">56</span>
<span id="L57" rel="#L57">57</span>
<span id="L58" rel="#L58">58</span>
<span id="L59" rel="#L59">59</span>
<span id="L60" rel="#L60">60</span>
<span id="L61" rel="#L61">61</span>
<span id="L62" rel="#L62">62</span>
<span id="L63" rel="#L63">63</span>
<span id="L64" rel="#L64">64</span>
<span id="L65" rel="#L65">65</span>
<span id="L66" rel="#L66">66</span>
<span id="L67" rel="#L67">67</span>
<span id="L68" rel="#L68">68</span>
<span id="L69" rel="#L69">69</span>
<span id="L70" rel="#L70">70</span>
<span id="L71" rel="#L71">71</span>
<span id="L72" rel="#L72">72</span>
<span id="L73" rel="#L73">73</span>
<span id="L74" rel="#L74">74</span>
<span id="L75" rel="#L75">75</span>
<span id="L76" rel="#L76">76</span>
<span id="L77" rel="#L77">77</span>
<span id="L78" rel="#L78">78</span>
<span id="L79" rel="#L79">79</span>
<span id="L80" rel="#L80">80</span>
<span id="L81" rel="#L81">81</span>
<span id="L82" rel="#L82">82</span>
<span id="L83" rel="#L83">83</span>
<span id="L84" rel="#L84">84</span>
<span id="L85" rel="#L85">85</span>
<span id="L86" rel="#L86">86</span>
<span id="L87" rel="#L87">87</span>
<span id="L88" rel="#L88">88</span>
<span id="L89" rel="#L89">89</span>
<span id="L90" rel="#L90">90</span>
<span id="L91" rel="#L91">91</span>
<span id="L92" rel="#L92">92</span>
<span id="L93" rel="#L93">93</span>
<span id="L94" rel="#L94">94</span>
<span id="L95" rel="#L95">95</span>
<span id="L96" rel="#L96">96</span>
<span id="L97" rel="#L97">97</span>
<span id="L98" rel="#L98">98</span>
<span id="L99" rel="#L99">99</span>
<span id="L100" rel="#L100">100</span>
<span id="L101" rel="#L101">101</span>
<span id="L102" rel="#L102">102</span>
<span id="L103" rel="#L103">103</span>
<span id="L104" rel="#L104">104</span>
<span id="L105" rel="#L105">105</span>
<span id="L106" rel="#L106">106</span>
<span id="L107" rel="#L107">107</span>
<span id="L108" rel="#L108">108</span>
<span id="L109" rel="#L109">109</span>
<span id="L110" rel="#L110">110</span>
<span id="L111" rel="#L111">111</span>
<span id="L112" rel="#L112">112</span>
<span id="L113" rel="#L113">113</span>
<span id="L114" rel="#L114">114</span>
<span id="L115" rel="#L115">115</span>
<span id="L116" rel="#L116">116</span>
<span id="L117" rel="#L117">117</span>
<span id="L118" rel="#L118">118</span>
<span id="L119" rel="#L119">119</span>
<span id="L120" rel="#L120">120</span>
<span id="L121" rel="#L121">121</span>
<span id="L122" rel="#L122">122</span>
<span id="L123" rel="#L123">123</span>
<span id="L124" rel="#L124">124</span>
<span id="L125" rel="#L125">125</span>
<span id="L126" rel="#L126">126</span>
<span id="L127" rel="#L127">127</span>
<span id="L128" rel="#L128">128</span>
<span id="L129" rel="#L129">129</span>
<span id="L130" rel="#L130">130</span>
<span id="L131" rel="#L131">131</span>
<span id="L132" rel="#L132">132</span>
<span id="L133" rel="#L133">133</span>
<span id="L134" rel="#L134">134</span>
<span id="L135" rel="#L135">135</span>
<span id="L136" rel="#L136">136</span>
<span id="L137" rel="#L137">137</span>
<span id="L138" rel="#L138">138</span>
<span id="L139" rel="#L139">139</span>
<span id="L140" rel="#L140">140</span>
<span id="L141" rel="#L141">141</span>
<span id="L142" rel="#L142">142</span>
<span id="L143" rel="#L143">143</span>
<span id="L144" rel="#L144">144</span>
<span id="L145" rel="#L145">145</span>
<span id="L146" rel="#L146">146</span>
<span id="L147" rel="#L147">147</span>
<span id="L148" rel="#L148">148</span>
<span id="L149" rel="#L149">149</span>
<span id="L150" rel="#L150">150</span>
<span id="L151" rel="#L151">151</span>
<span id="L152" rel="#L152">152</span>
<span id="L153" rel="#L153">153</span>
<span id="L154" rel="#L154">154</span>
<span id="L155" rel="#L155">155</span>
<span id="L156" rel="#L156">156</span>
<span id="L157" rel="#L157">157</span>
<span id="L158" rel="#L158">158</span>
<span id="L159" rel="#L159">159</span>
<span id="L160" rel="#L160">160</span>
<span id="L161" rel="#L161">161</span>
<span id="L162" rel="#L162">162</span>
<span id="L163" rel="#L163">163</span>
<span id="L164" rel="#L164">164</span>
<span id="L165" rel="#L165">165</span>
<span id="L166" rel="#L166">166</span>
<span id="L167" rel="#L167">167</span>
<span id="L168" rel="#L168">168</span>
<span id="L169" rel="#L169">169</span>
<span id="L170" rel="#L170">170</span>
<span id="L171" rel="#L171">171</span>
<span id="L172" rel="#L172">172</span>
<span id="L173" rel="#L173">173</span>
<span id="L174" rel="#L174">174</span>
<span id="L175" rel="#L175">175</span>
<span id="L176" rel="#L176">176</span>
<span id="L177" rel="#L177">177</span>
<span id="L178" rel="#L178">178</span>
<span id="L179" rel="#L179">179</span>
<span id="L180" rel="#L180">180</span>
<span id="L181" rel="#L181">181</span>
<span id="L182" rel="#L182">182</span>
<span id="L183" rel="#L183">183</span>
<span id="L184" rel="#L184">184</span>

           </td>
           <td class="blob-line-code"><div class="code-body highlight"><pre><div class='line' id='LC1'><span class="c1">// Script set up, helpers and libs</span></div><div class='line' id='LC2'><span class="kd">var</span> <span class="nx">fs</span> <span class="o">=</span> <span class="nx">require</span><span class="p">(</span><span class="s1">&#39;fs&#39;</span><span class="p">),</span></div><div class='line' id='LC3'>	<span class="nx">system</span> <span class="o">=</span> <span class="nx">require</span><span class="p">(</span><span class="s1">&#39;system&#39;</span><span class="p">),</span>	</div><div class='line' id='LC4'>	<span class="nx">casper</span> <span class="o">=</span> <span class="nx">require</span><span class="p">(</span><span class="s1">&#39;casper&#39;</span><span class="p">).</span><span class="nx">create</span><span class="p">({</span></div><div class='line' id='LC5'>		<span class="nx">logLevel</span><span class="o">:</span> <span class="s2">&quot;error&quot;</span></div><div class='line' id='LC6'>	<span class="p">});</span></div><div class='line' id='LC7'><br/></div><div class='line' id='LC8'><span class="c1">// Get the party started</span></div><div class='line' id='LC9'><span class="kd">var</span> <span class="nx">application</span> <span class="o">=</span> <span class="p">{</span></div><div class='line' id='LC10'><br/></div><div class='line' id='LC11'>	<span class="nx">global</span> <span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC12'>		<span class="nx">inputSource</span> <span class="o">:</span> <span class="s2">&quot;&quot;</span><span class="p">,</span> <span class="c1">// Defined by user prompt</span></div><div class='line' id='LC13'>		<span class="nx">outputFile</span> <span class="o">:</span> <span class="s2">&quot;&quot;</span><span class="p">,</span> <span class="c1">// Defined by user prompt</span></div><div class='line' id='LC14'>		<span class="nx">result</span> <span class="o">:</span> <span class="s2">&quot;&quot;</span><span class="p">,</span> <span class="c1">// Generated when page is processed</span></div><div class='line' id='LC15'>		<span class="nx">browserSize</span> <span class="o">:</span> <span class="p">[]</span> <span class="c1">// Defined by user prompt</span></div><div class='line' id='LC16'>	<span class="p">},</span></div><div class='line' id='LC17'><br/></div><div class='line' id='LC18'>	<span class="nx">init</span> <span class="o">:</span> <span class="kd">function</span><span class="p">(){</span></div><div class='line' id='LC19'>		<span class="kd">var</span> <span class="nx">self</span> <span class="o">=</span> <span class="k">this</span><span class="p">;</span></div><div class='line' id='LC20'><br/></div><div class='line' id='LC21'>		<span class="nx">self</span><span class="p">.</span><span class="nx">setGlobal</span><span class="p">();</span></div><div class='line' id='LC22'><br/></div><div class='line' id='LC23'>	<span class="p">},</span> <span class="c1">// init</span></div><div class='line' id='LC24'><br/></div><div class='line' id='LC25'>	<span class="nx">setGlobal</span> <span class="o">:</span> <span class="kd">function</span><span class="p">(){</span></div><div class='line' id='LC26'>		<span class="kd">var</span> <span class="nx">self</span> <span class="o">=</span> <span class="k">this</span><span class="p">;</span></div><div class='line' id='LC27'><br/></div><div class='line' id='LC28'>		<span class="c1">// Set the input URL</span></div><div class='line' id='LC29'>		<span class="nx">system</span><span class="p">.</span><span class="nx">stdout</span><span class="p">.</span><span class="nx">writeLine</span><span class="p">(</span><span class="s2">&quot;Which URL would you like to parse?&quot;</span><span class="p">);</span></div><div class='line' id='LC30'>		<span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">inputSource</span> <span class="o">=</span> <span class="nx">system</span><span class="p">.</span><span class="nx">stdin</span><span class="p">.</span><span class="nx">readLine</span><span class="p">().</span><span class="nx">replace</span><span class="p">(</span><span class="s2">&quot;\n&quot;</span><span class="p">,</span><span class="s2">&quot;&quot;</span><span class="p">);</span></div><div class='line' id='LC31'><br/></div><div class='line' id='LC32'>		<span class="c1">// Set the output file</span></div><div class='line' id='LC33'>		<span class="nx">system</span><span class="p">.</span><span class="nx">stdout</span><span class="p">.</span><span class="nx">writeLine</span><span class="p">(</span><span class="s2">&quot;Which file would you like to output the result to? (leave blank for critical-css.txt)&quot;</span><span class="p">);</span></div><div class='line' id='LC34'>		<span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">outputFile</span> <span class="o">=</span> <span class="nx">system</span><span class="p">.</span><span class="nx">stdin</span><span class="p">.</span><span class="nx">readLine</span><span class="p">().</span><span class="nx">replace</span><span class="p">(</span><span class="s2">&quot;\n&quot;</span><span class="p">,</span><span class="s2">&quot;&quot;</span><span class="p">);</span></div><div class='line' id='LC35'><br/></div><div class='line' id='LC36'>		<span class="c1">// If the output is blank, use the default one</span></div><div class='line' id='LC37'>		<span class="k">if</span><span class="p">(</span><span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">outputFile</span> <span class="o">==</span> <span class="s2">&quot;&quot;</span><span class="p">){</span></div><div class='line' id='LC38'><br/></div><div class='line' id='LC39'>			<span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">outputFile</span> <span class="o">=</span> <span class="s2">&quot;critical-css.txt&quot;</span><span class="p">;</span></div><div class='line' id='LC40'><br/></div><div class='line' id='LC41'>		<span class="p">};</span></div><div class='line' id='LC42'><br/></div><div class='line' id='LC43'>		<span class="c1">// Set the browser dimensions</span></div><div class='line' id='LC44'>		<span class="nx">system</span><span class="p">.</span><span class="nx">stdout</span><span class="p">.</span><span class="nx">writeLine</span><span class="p">(</span><span class="s2">&quot;What size would you like the browser to be? (Expected input is widthxheight) (leave blank for 1024x768)&quot;</span><span class="p">);</span></div><div class='line' id='LC45'>		<span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span> <span class="o">=</span> <span class="nx">system</span><span class="p">.</span><span class="nx">stdin</span><span class="p">.</span><span class="nx">readLine</span><span class="p">().</span><span class="nx">replace</span><span class="p">(</span><span class="s2">&quot;\n&quot;</span><span class="p">,</span><span class="s2">&quot;&quot;</span><span class="p">);</span></div><div class='line' id='LC46'><br/></div><div class='line' id='LC47'>		<span class="c1">// If the output is blank, use the default one</span></div><div class='line' id='LC48'>		<span class="k">if</span><span class="p">(</span><span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span> <span class="o">==</span> <span class="s2">&quot;&quot;</span><span class="p">){</span></div><div class='line' id='LC49'><br/></div><div class='line' id='LC50'>			<span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span> <span class="o">=</span> <span class="s2">&quot;1024x768&quot;</span><span class="p">;</span></div><div class='line' id='LC51'><br/></div><div class='line' id='LC52'>		<span class="p">};</span></div><div class='line' id='LC53'><br/></div><div class='line' id='LC54'>		<span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span> <span class="o">=</span> <span class="nx">self</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span><span class="p">.</span><span class="nx">split</span><span class="p">(</span><span class="s2">&quot;x&quot;</span><span class="p">);</span></div><div class='line' id='LC55'><br/></div><div class='line' id='LC56'>		<span class="nx">self</span><span class="p">.</span><span class="nx">parseCSS</span><span class="p">();</span></div><div class='line' id='LC57'><br/></div><div class='line' id='LC58'>	<span class="p">},</span> <span class="c1">// setGlobal</span></div><div class='line' id='LC59'><br/></div><div class='line' id='LC60'>	<span class="nx">parseCSS</span> <span class="o">:</span> <span class="kd">function</span><span class="p">(){</span></div><div class='line' id='LC61'><br/></div><div class='line' id='LC62'>		<span class="nx">casper</span><span class="p">.</span><span class="nx">start</span><span class="p">();</span></div><div class='line' id='LC63'><br/></div><div class='line' id='LC64'>		<span class="nx">casper</span><span class="p">.</span><span class="nx">then</span><span class="p">(</span><span class="kd">function</span><span class="p">()</span> <span class="p">{</span></div><div class='line' id='LC65'><br/></div><div class='line' id='LC66'>			<span class="k">if</span><span class="p">(</span><span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">inputSource</span> <span class="o">==</span> <span class="s2">&quot;&quot;</span><span class="p">){</span></div><div class='line' id='LC67'>				<span class="k">this</span><span class="p">.</span><span class="nx">die</span><span class="p">(</span><span class="s2">&quot;No URL defined, try again.&quot;</span><span class="p">,</span> <span class="mi">1</span><span class="p">);</span></div><div class='line' id='LC68'>			<span class="p">};</span></div><div class='line' id='LC69'><br/></div><div class='line' id='LC70'>			<span class="c1">// Set the viewport</span></div><div class='line' id='LC71'>			<span class="nx">casper</span><span class="p">.</span><span class="nx">viewport</span><span class="p">(</span><span class="nb">parseFloat</span><span class="p">(</span><span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span><span class="p">[</span><span class="mi">0</span><span class="p">]),</span> <span class="nb">parseFloat</span><span class="p">(</span><span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span><span class="p">[</span><span class="mi">1</span><span class="p">]));</span></div><div class='line' id='LC72'><br/></div><div class='line' id='LC73'>			<span class="k">this</span><span class="p">.</span><span class="nx">echo</span><span class="p">(</span><span class="s2">&quot;Processing: &quot;</span> <span class="o">+</span> <span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">inputSource</span><span class="p">,</span> <span class="s2">&quot;INFO&quot;</span><span class="p">);</span></div><div class='line' id='LC74'>			<span class="k">this</span><span class="p">.</span><span class="nx">echo</span><span class="p">(</span><span class="s2">&quot;Rendering page at: &quot;</span> <span class="o">+</span> <span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="o">+</span> <span class="s2">&quot; x &quot;</span> <span class="o">+</span> <span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">browserSize</span><span class="p">[</span><span class="mi">1</span><span class="p">],</span> <span class="s2">&quot;INFO&quot;</span><span class="p">);</span></div><div class='line' id='LC75'><br/></div><div class='line' id='LC76'>			<span class="k">this</span><span class="p">.</span><span class="nx">thenOpen</span><span class="p">(</span><span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">inputSource</span><span class="p">,</span> <span class="kd">function</span><span class="p">(){</span></div><div class='line' id='LC77'><br/></div><div class='line' id='LC78'>				<span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">result</span> <span class="o">=</span> <span class="k">this</span><span class="p">.</span><span class="nx">evaluate</span><span class="p">(</span><span class="kd">function</span><span class="p">(){</span></div><div class='line' id='LC79'><br/></div><div class='line' id='LC80'>					<span class="kd">var</span> <span class="nx">CSSCriticalPath</span> <span class="o">=</span> <span class="kd">function</span><span class="p">(</span><span class="nx">w</span><span class="p">,</span> <span class="nx">d</span><span class="p">,</span> <span class="nx">opts</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC81'>						<span class="kd">var</span> <span class="nx">opt</span> <span class="o">=</span> <span class="nx">opts</span> <span class="o">||</span> <span class="p">{};</span></div><div class='line' id='LC82'>						<span class="kd">var</span> <span class="nx">css</span> <span class="o">=</span> <span class="p">{};</span></div><div class='line' id='LC83'>						<span class="kd">var</span> <span class="nx">pushCSS</span> <span class="o">=</span> <span class="kd">function</span><span class="p">(</span><span class="nx">r</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC84'>							<span class="k">if</span><span class="p">(</span><span class="o">!!</span><span class="nx">css</span><span class="p">[</span><span class="nx">r</span><span class="p">.</span><span class="nx">selectorText</span><span class="p">]</span> <span class="o">===</span> <span class="kc">false</span><span class="p">)</span> <span class="nx">css</span><span class="p">[</span><span class="nx">r</span><span class="p">.</span><span class="nx">selectorText</span><span class="p">]</span> <span class="o">=</span> <span class="p">{};</span></div><div class='line' id='LC85'>							<span class="kd">var</span> <span class="nx">styles</span> <span class="o">=</span> <span class="nx">r</span><span class="p">.</span><span class="nx">style</span><span class="p">.</span><span class="nx">cssText</span><span class="p">.</span><span class="nx">split</span><span class="p">(</span><span class="sr">/;(?![A-Za-z0-9])/</span><span class="p">);</span></div><div class='line' id='LC86'>							<span class="k">for</span><span class="p">(</span><span class="kd">var</span> <span class="nx">i</span> <span class="o">=</span> <span class="mi">0</span><span class="p">;</span> <span class="nx">i</span> <span class="o">&lt;</span> <span class="nx">styles</span><span class="p">.</span><span class="nx">length</span><span class="p">;</span> <span class="nx">i</span><span class="o">++</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC87'>								<span class="k">if</span><span class="p">(</span><span class="o">!!</span><span class="nx">styles</span><span class="p">[</span><span class="nx">i</span><span class="p">]</span> <span class="o">===</span> <span class="kc">false</span><span class="p">)</span> <span class="k">continue</span><span class="p">;</span></div><div class='line' id='LC88'>								<span class="c1">// Note - altered &#39;: &#39; to be &#39;:&#39; from the original source.</span></div><div class='line' id='LC89'>								<span class="c1">// PhantomJS doesn&#39;t return the space after the &#39;:&#39;</span></div><div class='line' id='LC90'>								<span class="c1">// Also altered the split to be only the first &#39;:&#39;, as</span></div><div class='line' id='LC91'>								<span class="c1">// things like &#39;background:url(http://&#39; would split in</span></div><div class='line' id='LC92'>								<span class="c1">// 3 parts. And aint nobody got time for that.</span></div><div class='line' id='LC93'>								<span class="kd">var</span> <span class="nx">pair</span> <span class="o">=</span> <span class="nx">styles</span><span class="p">[</span><span class="nx">i</span><span class="p">].</span><span class="nx">split</span><span class="p">(</span><span class="sr">/:(.+)?/</span><span class="p">);</span></div><div class='line' id='LC94'><br/></div><div class='line' id='LC95'>								<span class="c1">// Have added this if statement in order for it to work</span></div><div class='line' id='LC96'>								<span class="c1">// within CasperJS.</span></div><div class='line' id='LC97'>								<span class="c1">// Not sure why, but sometimes pair[1] is undefined.</span></div><div class='line' id='LC98'>								<span class="k">if</span><span class="p">(</span><span class="k">typeof</span><span class="p">(</span><span class="nx">pair</span><span class="p">[</span><span class="mi">1</span><span class="p">])</span> <span class="o">!=</span> <span class="s2">&quot;undefined&quot;</span><span class="p">){</span></div><div class='line' id='LC99'>									<span class="nx">pair</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="o">=</span> <span class="nx">pair</span><span class="p">[</span><span class="mi">0</span><span class="p">].</span><span class="nx">trim</span><span class="p">();</span></div><div class='line' id='LC100'>									<span class="nx">pair</span><span class="p">[</span><span class="mi">1</span><span class="p">]</span> <span class="o">=</span> <span class="nx">pair</span><span class="p">[</span><span class="mi">1</span><span class="p">].</span><span class="nx">trim</span><span class="p">();</span></div><div class='line' id='LC101'>									<span class="nx">css</span><span class="p">[</span><span class="nx">r</span><span class="p">.</span><span class="nx">selectorText</span><span class="p">][</span><span class="nx">pair</span><span class="p">[</span><span class="mi">0</span><span class="p">]]</span> <span class="o">=</span> <span class="nx">pair</span><span class="p">[</span><span class="mi">1</span><span class="p">];</span></div><div class='line' id='LC102'>								<span class="p">}</span></div><div class='line' id='LC103'>							<span class="p">}</span></div><div class='line' id='LC104'>						<span class="p">};</span></div><div class='line' id='LC105'><br/></div><div class='line' id='LC106'>						<span class="kd">var</span> <span class="nx">parseTree</span> <span class="o">=</span> <span class="kd">function</span><span class="p">()</span> <span class="p">{</span> </div><div class='line' id='LC107'>							<span class="kd">var</span> <span class="nx">height</span> <span class="o">=</span> <span class="nx">w</span><span class="p">.</span><span class="nx">innerHeight</span><span class="p">;</span></div><div class='line' id='LC108'>							<span class="kd">var</span> <span class="nx">walker</span> <span class="o">=</span> <span class="nx">d</span><span class="p">.</span><span class="nx">createTreeWalker</span><span class="p">(</span><span class="nx">d</span><span class="p">,</span> <span class="nx">NodeFilter</span><span class="p">.</span><span class="nx">SHOW_ELEMENT</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">node</span><span class="p">)</span> <span class="p">{</span> <span class="k">return</span> <span class="nx">NodeFilter</span><span class="p">.</span><span class="nx">FILTER_ACCEPT</span><span class="p">;</span> <span class="p">},</span> <span class="kc">true</span><span class="p">);</span></div><div class='line' id='LC109'><br/></div><div class='line' id='LC110'>							<span class="k">while</span><span class="p">(</span><span class="nx">walker</span><span class="p">.</span><span class="nx">nextNode</span><span class="p">())</span> <span class="p">{</span></div><div class='line' id='LC111'>								<span class="kd">var</span> <span class="nx">node</span> <span class="o">=</span> <span class="nx">walker</span><span class="p">.</span><span class="nx">currentNode</span><span class="p">;</span></div><div class='line' id='LC112'>								<span class="kd">var</span> <span class="nx">rect</span> <span class="o">=</span> <span class="nx">node</span><span class="p">.</span><span class="nx">getBoundingClientRect</span><span class="p">();</span></div><div class='line' id='LC113'>								<span class="k">if</span><span class="p">(</span><span class="nx">rect</span><span class="p">.</span><span class="nx">top</span> <span class="o">&lt;</span> <span class="nx">height</span> <span class="o">||</span> <span class="nx">opt</span><span class="p">.</span><span class="nx">scanFullPage</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC114'>									<span class="kd">var</span> <span class="nx">rules</span> <span class="o">=</span> <span class="nx">w</span><span class="p">.</span><span class="nx">getMatchedCSSRules</span><span class="p">(</span><span class="nx">node</span><span class="p">);</span></div><div class='line' id='LC115'>									<span class="k">if</span><span class="p">(</span><span class="o">!!</span><span class="nx">rules</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC116'>										<span class="k">for</span><span class="p">(</span><span class="kd">var</span> <span class="nx">r</span> <span class="o">=</span> <span class="mi">0</span><span class="p">;</span> <span class="nx">r</span> <span class="o">&lt;</span> <span class="nx">rules</span><span class="p">.</span><span class="nx">length</span><span class="p">;</span> <span class="nx">r</span><span class="o">++</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC117'>											<span class="nx">pushCSS</span><span class="p">(</span><span class="nx">rules</span><span class="p">[</span><span class="nx">r</span><span class="p">]);</span> </div><div class='line' id='LC118'>										<span class="p">}</span></div><div class='line' id='LC119'>									<span class="p">}</span></div><div class='line' id='LC120'>								<span class="p">}</span> </div><div class='line' id='LC121'>							<span class="p">}</span></div><div class='line' id='LC122'>						<span class="p">};</span></div><div class='line' id='LC123'><br/></div><div class='line' id='LC124'>						<span class="k">this</span><span class="p">.</span><span class="nx">generateCSS</span> <span class="o">=</span> <span class="kd">function</span><span class="p">()</span> <span class="p">{</span></div><div class='line' id='LC125'>							<span class="kd">var</span> <span class="nx">finalCSS</span> <span class="o">=</span> <span class="s2">&quot;&quot;</span><span class="p">;</span></div><div class='line' id='LC126'>							<span class="k">for</span><span class="p">(</span><span class="kd">var</span> <span class="nx">k</span> <span class="k">in</span> <span class="nx">css</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC127'>								<span class="nx">finalCSS</span> <span class="o">+=</span> <span class="nx">k</span> <span class="o">+</span> <span class="s2">&quot; { &quot;</span><span class="p">;</span></div><div class='line' id='LC128'>								<span class="k">for</span><span class="p">(</span><span class="kd">var</span> <span class="nx">j</span> <span class="k">in</span> <span class="nx">css</span><span class="p">[</span><span class="nx">k</span><span class="p">])</span> <span class="p">{</span></div><div class='line' id='LC129'>									<span class="nx">finalCSS</span> <span class="o">+=</span> <span class="nx">j</span> <span class="o">+</span> <span class="s2">&quot;: &quot;</span> <span class="o">+</span> <span class="nx">css</span><span class="p">[</span><span class="nx">k</span><span class="p">][</span><span class="nx">j</span><span class="p">]</span> <span class="o">+</span> <span class="s2">&quot;; &quot;</span><span class="p">;</span></div><div class='line' id='LC130'>								<span class="p">}</span></div><div class='line' id='LC131'>								<span class="nx">finalCSS</span> <span class="o">+=</span> <span class="s2">&quot;}\n&quot;</span><span class="p">;</span></div><div class='line' id='LC132'>							<span class="p">}</span></div><div class='line' id='LC133'><br/></div><div class='line' id='LC134'>							<span class="k">return</span> <span class="nx">finalCSS</span><span class="p">;</span></div><div class='line' id='LC135'>						<span class="p">};</span></div><div class='line' id='LC136'><br/></div><div class='line' id='LC137'>						<span class="nx">parseTree</span><span class="p">();</span></div><div class='line' id='LC138'>					<span class="p">};</span></div><div class='line' id='LC139'><br/></div><div class='line' id='LC140'><br/></div><div class='line' id='LC141'>					<span class="kd">var</span> <span class="nx">cp</span> <span class="o">=</span> <span class="k">new</span> <span class="nx">CSSCriticalPath</span><span class="p">(</span><span class="nb">window</span><span class="p">,</span> <span class="nb">document</span><span class="p">);</span></div><div class='line' id='LC142'>					<span class="kd">var</span> <span class="nx">css</span> <span class="o">=</span> <span class="nx">cp</span><span class="p">.</span><span class="nx">generateCSS</span><span class="p">();</span></div><div class='line' id='LC143'><br/></div><div class='line' id='LC144'>					<span class="k">return</span> <span class="nx">css</span><span class="p">;</span></div><div class='line' id='LC145'><br/></div><div class='line' id='LC146'>				<span class="p">});</span></div><div class='line' id='LC147'><br/></div><div class='line' id='LC148'>				<span class="k">this</span><span class="p">.</span><span class="nx">echo</span><span class="p">(</span><span class="s2">&quot;CSS complete, &quot;</span> <span class="o">+</span> <span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">result</span><span class="p">.</span><span class="nx">length</span> <span class="o">+</span> <span class="s2">&quot; characters long&quot;</span><span class="p">,</span> <span class="s2">&quot;INFO&quot;</span><span class="p">);</span></div><div class='line' id='LC149'><br/></div><div class='line' id='LC150'>			<span class="p">});</span></div><div class='line' id='LC151'><br/></div><div class='line' id='LC152'>		<span class="p">});</span></div><div class='line' id='LC153'><br/></div><div class='line' id='LC154'>		<span class="nx">casper</span><span class="p">.</span><span class="nx">then</span><span class="p">(</span><span class="kd">function</span><span class="p">(){</span></div><div class='line' id='LC155'><br/></div><div class='line' id='LC156'>			<span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">result</span> <span class="o">=</span> <span class="s2">&quot;&lt;style&gt;&quot;</span> <span class="o">+</span> <span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">result</span> <span class="o">+</span> <span class="s2">&quot;&lt;/style&gt;&quot;</span><span class="p">;</span></div><div class='line' id='LC157'><br/></div><div class='line' id='LC158'>			<span class="nx">fs</span><span class="p">.</span><span class="nx">write</span><span class="p">(</span><span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">outputFile</span><span class="p">,</span> <span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">result</span><span class="p">,</span> <span class="s1">&#39;w&#39;</span><span class="p">);</span></div><div class='line' id='LC159'><br/></div><div class='line' id='LC160'>			<span class="k">this</span><span class="p">.</span><span class="nx">echo</span><span class="p">(</span><span class="s2">&quot;Results file complete: &quot;</span> <span class="o">+</span> <span class="nx">application</span><span class="p">.</span><span class="nx">global</span><span class="p">.</span><span class="nx">outputFile</span><span class="p">,</span> <span class="s2">&quot;INFO&quot;</span><span class="p">);</span></div><div class='line' id='LC161'><br/></div><div class='line' id='LC162'>		<span class="p">});</span></div><div class='line' id='LC163'><br/></div><div class='line' id='LC164'>		<span class="nx">casper</span><span class="p">.</span><span class="nx">on</span><span class="p">(</span><span class="s2">&quot;page.error&quot;</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span><span class="nx">msg</span><span class="p">,</span> <span class="nx">trace</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC165'>			<span class="k">this</span><span class="p">.</span><span class="nx">echo</span><span class="p">(</span><span class="s2">&quot;Error:    &quot;</span> <span class="o">+</span> <span class="nx">msg</span><span class="p">,</span> <span class="s2">&quot;ERROR&quot;</span><span class="p">);</span></div><div class='line' id='LC166'>			<span class="k">this</span><span class="p">.</span><span class="nx">echo</span><span class="p">(</span><span class="s2">&quot;file:     &quot;</span> <span class="o">+</span> <span class="nx">trace</span><span class="p">[</span><span class="mi">0</span><span class="p">].</span><span class="nx">file</span><span class="p">,</span> <span class="s2">&quot;WARNING&quot;</span><span class="p">);</span></div><div class='line' id='LC167'>			<span class="k">this</span><span class="p">.</span><span class="nx">echo</span><span class="p">(</span><span class="s2">&quot;line:     &quot;</span> <span class="o">+</span> <span class="nx">trace</span><span class="p">[</span><span class="mi">0</span><span class="p">].</span><span class="nx">line</span><span class="p">,</span> <span class="s2">&quot;WARNING&quot;</span><span class="p">);</span></div><div class='line' id='LC168'>			<span class="k">this</span><span class="p">.</span><span class="nx">echo</span><span class="p">(</span><span class="s2">&quot;function: &quot;</span> <span class="o">+</span> <span class="nx">trace</span><span class="p">[</span><span class="mi">0</span><span class="p">][</span><span class="s2">&quot;function&quot;</span><span class="p">],</span> <span class="s2">&quot;WARNING&quot;</span><span class="p">);</span></div><div class='line' id='LC169'>		<span class="p">});</span></div><div class='line' id='LC170'><br/></div><div class='line' id='LC171'>		<span class="nx">casper</span><span class="p">.</span><span class="nx">run</span><span class="p">();</span></div><div class='line' id='LC172'><br/></div><div class='line' id='LC173'>	<span class="p">}</span> <span class="c1">// parseCSS</span></div><div class='line' id='LC174'><br/></div><div class='line' id='LC175'><span class="p">}</span> <span class="c1">// application</span></div><div class='line' id='LC176'><br/></div><div class='line' id='LC177'><span class="nx">application</span><span class="p">.</span><span class="nx">init</span><span class="p">();</span></div><div class='line' id='LC178'><br/></div><div class='line' id='LC179'><span class="cm">/*</span></div><div class='line' id='LC180'><span class="cm">	Source: https://gist.github.com/PaulKinlan/6284142</span></div><div class='line' id='LC181'><span class="cm">*/</span></div><div class='line' id='LC182'><span class="kd">var</span> <span class="nx">criticalcss</span> <span class="o">=</span> <span class="kd">function</span><span class="p">(){</span></div><div class='line' id='LC183'><br/></div><div class='line' id='LC184'><span class="p">};</span></div></pre></div></td>
         </tr>
       </table>
  </div>

  </div>
</div>

<a href="#jump-to-line" rel="facebox[.linejump]" data-hotkey="l" class="js-jump-to-line" style="display:none">Jump to Line</a>
<div id="jump-to-line" style="display:none">
  <form accept-charset="UTF-8" class="js-jump-to-line-form">
    <input class="linejump-input js-jump-to-line-field" type="text" placeholder="Jump to line&hellip;" autofocus>
    <button type="submit" class="button">Go</button>
  </form>
</div>

        </div>

      </div><!-- /.repo-container -->
      <div class="modal-backdrop"></div>
    </div><!-- /.container -->
  </div><!-- /.site -->


    </div><!-- /.wrapper -->

      <div class="container">
  <div class="site-footer">
    <ul class="site-footer-links right">
      <li><a href="https://status.github.com/">Status</a></li>
      <li><a href="http://developer.github.com">API</a></li>
      <li><a href="http://training.github.com">Training</a></li>
      <li><a href="http://shop.github.com">Shop</a></li>
      <li><a href="/blog">Blog</a></li>
      <li><a href="/about">About</a></li>

    </ul>

    <a href="/">
      <span class="mega-octicon octicon-mark-github" title="GitHub"></span>
    </a>

    <ul class="site-footer-links">
      <li>&copy; 2014 <span title="0.05836s from github-fe121-cp1-prd.iad.github.net">GitHub</span>, Inc.</li>
        <li><a href="/site/terms">Terms</a></li>
        <li><a href="/site/privacy">Privacy</a></li>
        <li><a href="/security">Security</a></li>
        <li><a href="/contact">Contact</a></li>
    </ul>
  </div><!-- /.site-footer -->
</div><!-- /.container -->


    <div class="fullscreen-overlay js-fullscreen-overlay" id="fullscreen_overlay">
  <div class="fullscreen-container js-fullscreen-container">
    <div class="textarea-wrap">
      <textarea name="fullscreen-contents" id="fullscreen-contents" class="fullscreen-contents js-fullscreen-contents" placeholder="" data-suggester="fullscreen_suggester"></textarea>
    </div>
  </div>
  <div class="fullscreen-sidebar">
    <a href="#" class="exit-fullscreen js-exit-fullscreen tooltipped tooltipped-w" aria-label="Exit Zen Mode">
      <span class="mega-octicon octicon-screen-normal"></span>
    </a>
    <a href="#" class="theme-switcher js-theme-switcher tooltipped tooltipped-w"
      aria-label="Switch themes">
      <span class="octicon octicon-color-mode"></span>
    </a>
  </div>
</div>



    <div id="ajax-error-message" class="flash flash-error">
      <span class="octicon octicon-alert"></span>
      <a href="#" class="octicon octicon-x close js-ajax-error-dismiss"></a>
      Something went wrong with that request. Please try again.
    </div>


      <script crossorigin="anonymous" src="https://assets-cdn.github.com/assets/frameworks-e87aa86ffae369acf33a96bb6567b2b57183be57.js" type="text/javascript"></script>
      <script async="async" crossorigin="anonymous" src="https://assets-cdn.github.com/assets/github-41a23288068e4484cfbf360335086f7062a5c29f.js" type="text/javascript"></script>
      
      
        <script async src="https://www.google-analytics.com/analytics.js"></script>
  </body>
</html>

